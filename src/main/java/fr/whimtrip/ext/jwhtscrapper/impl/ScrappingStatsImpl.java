package fr.whimtrip.ext.jwhtscrapper.impl;

import fr.whimtrip.ext.jwhtscrapper.intfr.AutomaticScrapperClient;
import fr.whimtrip.ext.jwhtscrapper.intfr.ScrappingStats;

/**
 * <p>Part of project jwht-scrapper</p>
 * <p>Created on 26/07/18</p>
 *
 * <p>
 *     {@link ScrappingStats} default implementation. This
 *     implementation is immutable. Another mutable and live
 *     update implementation might be developped with a custom
 *     {@link AutomaticScrapperClient} implementation.
 * </p>
 *
 * @author Louis-wht
 * @since 1.0.0
 */
public final class ScrappingStatsImpl implements ScrappingStats {

    private final int finishedTasks;
    private final int runningTasks;
    private final int validFinishedTasks;
    private final int failedFinishedTasks;
    private final int remaining;

    /**
     * Default and only valid constructor. Will assign all statistics to
     * the current {@link ScrappingStats} implementation.
     * @param finishedTasks {@link ScrappingStats#getFinishedTasks()}
     * @param runningTasks {@link ScrappingStats#getRunningTasks()}
     * @param validFinishedTasks {@link ScrappingStats#getSuccessfullTasks()}
     * @param failedFinishedTasks {@link ScrappingStats#getFailedTasks()}
     * @param remaining {@link ScrappingStats#getRemaining()}
     */
    public ScrappingStatsImpl(int finishedTasks, int runningTasks, int validFinishedTasks, int failedFinishedTasks, int remaining) {

        this.finishedTasks = finishedTasks;
        this.runningTasks = runningTasks;
        this.validFinishedTasks = validFinishedTasks;
        this.failedFinishedTasks = failedFinishedTasks;
        this.remaining = remaining;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFinishedTasks() {

        return finishedTasks;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getRunningTasks() {

        return runningTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSuccessfullTasks() {

        return validFinishedTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFailedTasks() {

        return failedFinishedTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRemaining() {

        return remaining;
    }
}
