/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.service.scoped;

import fr.whimtrip.core.util.WhimtripUtils;
import fr.whimtrip.ext.jwhtscrapper.intfr.ExceptionLogger;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrapperHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by LOUISSTEIMBERG on 22/11/2017.
 */

/**
 *
 * @param <P> Parent Class
 * @param <M> Model on which HTML will be mapped
 */
public class AutomaticFieldScrapperClient<P, M> {

    private static final Logger log = LoggerFactory.getLogger(AutomaticFieldScrapperClient.class);
    private static final long SLEEP_TIME_BETWEEN_GATHERING_OF_RESULTS = 1000;
    private static final int LENGTH_OF_THE_PERCENTAGE_BAR = 100;
    private static final int WIDTH_OF_THE_PERCENTAGE_BAR = 2;
    private final ScrappingContext<?, P, M, ? extends ScrapperHelper<P, M>> context;

    private final HtmlAutoScrapper<M> htmlAutoScrapper;

    private final ExceptionLogger exceptionLoggerService;

    private final List<FutureTask<Object>> runningTasks = new ArrayList<>();

    private int finishedTasks = 0;


    public AutomaticFieldScrapperClient(
            ScrappingContext<?, P, M, ? extends ScrapperHelper<P, M>> context,
            HtmlAutoScrapper<M> htmlAutoScrapper,
            ExceptionLogger exceptionLoggerService
    )
    {
        this.context = context;
        this.htmlAutoScrapper = htmlAutoScrapper;
        this.exceptionLoggerService = exceptionLoggerService;
    }

    public List<Object> scrap() throws InterruptedException, ExecutionException
    {
        List results = new ArrayList<>();
        RequestScrappingContext requestScrappingContext = context.requestScrappingContext;
        List<P> plist = getParentAsList(context.getContainerObject());

        int numberOfScraps = 0;

        Iterator<P> iterator = plist.iterator();
        List<P> pSublist = new ArrayList<>();

        do {
            pSublist.clear();
            pSublist.addAll(newPSublist(numberOfScraps, requestScrappingContext, plist, iterator));
            iterator = plist.iterator();
            startThreads(numberOfScraps, pSublist);

            numberOfScraps += pSublist.size();

            ScrappingResult scrappingResult = emptyFinishedThreads(results, requestScrappingContext);
            log.info("nonScrappedThreads= {}, valids = {}.", scrappingResult.failed, scrappingResult.valid);

            int delay = context.getRequestScrappingContext().getRequestsConfig().periodicDelay();

            if(delay > 0 && scrappingResult.valid >= 1) {
                WhimtripUtils.waitForWithOutputToConsole((long)delay, 20);
            }

        } while(numberOfScraps < requestScrappingContext.getScrapLimit() && iterator.hasNext());

        while(!runningTasks.isEmpty())
        {
            emptyFinishedThreads(results, requestScrappingContext);
        }

        return results;
    }

    private ScrappingResult emptyFinishedThreads(List results, RequestScrappingContext requestScrappingContext)
            throws ExecutionException, InterruptedException {

        try {
            Thread.sleep(SLEEP_TIME_BETWEEN_GATHERING_OF_RESULTS);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        ScrappingResult scrappingResult = removeFinishedThreads(results, requestScrappingContext);

        int     percentageOfTasksFinished = (int)((float) (finishedTasks)
                                / (requestScrappingContext.getScrapLimit()) * 100 + 0.5),
                actualNumberOfBars = (int) (((float) percentageOfTasksFinished) / 100
                        * LENGTH_OF_THE_PERCENTAGE_BAR + 0.5);


        StringBuilder outBuilder = new StringBuilder();

        for (int j = 0; j < WIDTH_OF_THE_PERCENTAGE_BAR; j++) {
            outBuilder.append("||");
            for (int i = 1; i <= LENGTH_OF_THE_PERCENTAGE_BAR; i++) {
                if (i <= actualNumberOfBars)
                    outBuilder.append("#");
                else
                    outBuilder.append("-");
            }
            outBuilder.append("||\n");
        }

        System.out.println(outBuilder.toString());

        return scrappingResult;

    }


    private List<P> getParentAsList(Object container) {
        List<P> parentObjects = new ArrayList<>();
        if(container instanceof List)
        {
            parentObjects.addAll((List<P>) container);
        }
        else
        {
            parentObjects.add(((P) container));
        }

        return parentObjects;
    }




    private List<P> newPSublist(int numberOfScraps,
                                RequestScrappingContext requestScrappingContext,
                                List<P> pList,
                                Iterator<P> iterator)
    {
        List<P> pSublist = new ArrayList<>();
        int runningThreads = runningTasks.size();
        log.info("There are actually " + runningThreads + " running threads");
        List<P> copiedPList = new ArrayList<>();
        copiedPList.addAll(pList);

        while (numberOfScraps < requestScrappingContext.getScrapLimit()
                && runningThreads < requestScrappingContext.getParrallelThreads()
                && iterator.hasNext())
        {
            P p = iterator.next();
            pSublist.add(p);
            copiedPList.remove(p);
            numberOfScraps ++;
            runningThreads ++;
        }
        pList.clear();
        pList.addAll(copiedPList);
        return pSublist;
    }


    private ScrappingResult removeFinishedThreads(List results, RequestScrappingContext requestScrappingContext)
            throws ExecutionException, InterruptedException
    {
        List<FutureTask> copiedTasks = new ArrayList<>();
        copiedTasks.addAll(runningTasks);
        FutureTask<Object> actFt = null;
        ScrappingResult result = new ScrappingResult();
        for (FutureTask<Object> ft : copiedTasks) {
            actFt = ft;
            try {
                if(ft.isDone()) {

                    if(!((ScrapperFutureTask) ft).hasScrapped())
                    {
                        result.failed++;
                        log.info(String.format("Thread n°%s is terminated and wasn't scrapped", copiedTasks.indexOf(ft)));
                    }

                    else {
                        result.valid ++;
                        results.add(ft.get());
                        log.info(String.format("Thread n°%s is terminated and was correctly scrapped", copiedTasks.indexOf(ft)));
                    }


                    runningTasks.remove(ft);
                    finishedTasks ++;
                }
            } catch (InterruptedException | ExecutionException e) {
                finishedTasks ++;
                exceptionLoggerService.logException(e);
                log.info(String.format("Thread n°%s is terminated", copiedTasks.indexOf(ft)));
                runningTasks.remove(actFt);
                if (requestScrappingContext.isThrowExceptions()) {
                    throw e;
                }
            }

        }
        return result;
    }


    private List<FutureTask<Object>> startThreads(int offset, List<P> pSublist)
    {
        return startThreads(pSublist, pSublist.size() + 1, offset);
    }

    private List<FutureTask<Object>> startThreads(List<P> pSublist, int quantity)
    {
        return startThreads(pSublist, quantity, 0);
    }

    private List<FutureTask<Object>> startThreads(List<P> pSublist, int quantity, int offset)
    {
        List<FutureTask<Object>> fts = new ArrayList<>();

        int numberOfThreadsStarted = 0;
        for(P p : pSublist)
        {
            if(quantity > numberOfThreadsStarted)
            {
                FutureTask<Object> ft = new ScrapperFutureTask<>(
                        new ScrapperThreadCallable(p, context, htmlAutoScrapper)
                );
                fts.add(ft);
                runningTasks.add(ft);

                log.info(String.format("Starting thread n°%s", (offset + numberOfThreadsStarted) ));
            }
            else break;

            numberOfThreadsStarted++;
        }

        for(FutureTask<Object> ft : fts)
        {
            Thread t = new Thread(ft);
            t.start();
        }

        return fts;
    }

    public List<FutureTask<Object>> getRunningTasks() {
        return runningTasks;
    }

    public ScrappingContext<?, P, M, ? extends ScrapperHelper<P, M>> getContext() {
        return context;
    }

    private static class ScrappingResult{
        int valid = 0;
        int failed = 0;
    }

    public void stopRunningTasks() {
        for (FutureTask ft : runningTasks) {
            ft.cancel(true);
        }
    }


    public static class ScrapperFutureTask<P, M> extends FutureTask<Object>
    {
        private ScrapperThreadCallable<P,M> callable;

        public ScrapperFutureTask(@NotNull ScrapperThreadCallable<P, M> callable) {
            super(callable);
            this.callable = callable;
        }

        public boolean hasScrapped(){
            return callable.hasScrapped();
        }

        @Override
        public boolean isDone()
        {
            return callable.isDone();
        }
    }
}
