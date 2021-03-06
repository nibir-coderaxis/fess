/*
 * Copyright 2012-2016 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.helper;

import java.util.HashMap;
import java.util.Map;

import org.codelibs.core.lang.StringUtil;
import org.codelibs.fess.Constants;
import org.codelibs.fess.es.config.exbhv.JobLogBhv;
import org.codelibs.fess.es.config.exbhv.ScheduledJobBhv;
import org.codelibs.fess.es.config.exentity.JobLog;
import org.codelibs.fess.es.config.exentity.ScheduledJob;
import org.codelibs.fess.job.ScheduledJobException;
import org.codelibs.fess.mylasta.direction.FessConfig;
import org.codelibs.fess.util.ComponentUtil;
import org.dbflute.optional.OptionalThing;
import org.lastaflute.job.JobManager;
import org.lastaflute.job.LaCron;
import org.lastaflute.job.LaScheduledJob;
import org.lastaflute.job.key.LaJobUnique;
import org.lastaflute.job.subsidiary.CronParamsSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobHelper {
    private static final Logger logger = LoggerFactory.getLogger(JobHelper.class);

    public void register(final ScheduledJob scheduledJob) {
        final JobManager jobManager = ComponentUtil.getJobManager();
        jobManager.schedule(cron -> register(cron, scheduledJob));
    }

    public void register(final LaCron cron, final ScheduledJob scheduledJob) {
        if (scheduledJob == null) {
            throw new ScheduledJobException("No job.");
        }

        final String id = scheduledJob.getId();
        if (!Constants.T.equals(scheduledJob.getAvailable())) {
            logger.info("Inactive Job " + id + ":" + scheduledJob.getName());
            try {
                unregister(scheduledJob);
            } catch (final Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to delete Job " + scheduledJob, e);
                }
            }
            return;
        }

        final FessConfig fessConfig = ComponentUtil.getFessConfig();
        final CronParamsSupplier paramsOp = () -> {
            final Map<String, Object> params = new HashMap<>();
            params.put(Constants.SCHEDULED_JOB, scheduledJob);
            return params;
        };
        findJobByUniqueOf(LaJobUnique.of(id)).ifPresent(job -> {
            if (!job.isUnscheduled()) {
                if (StringUtil.isNotBlank(scheduledJob.getCronExpression())) {
                    logger.info("Starting Job " + id + ":" + scheduledJob.getName());
                    final String cronExpression = scheduledJob.getCronExpression();
                    job.reschedule(cronExpression, op -> op.changeNoticeLogToDebug().params(paramsOp));
                } else {
                    logger.info("Inactive Job " + id + ":" + scheduledJob.getName());
                    job.becomeNonCron();
                }
            } else if (StringUtil.isNotBlank(scheduledJob.getCronExpression())) {
                logger.info("Starting Job " + id + ":" + scheduledJob.getName());
                final String cronExpression = scheduledJob.getCronExpression();
                job.reschedule(cronExpression, op -> op.changeNoticeLogToDebug().params(paramsOp));
            }
        }).orElse(
                () -> {
                    if (StringUtil.isNotBlank(scheduledJob.getCronExpression())) {
                        logger.info("Starting Job " + id + ":" + scheduledJob.getName());
                        final String cronExpression = scheduledJob.getCronExpression();
                        cron.register(cronExpression, fessConfig.getSchedulerJobClassAsClass(),
                                fessConfig.getSchedulerConcurrentExecModeAsEnum(),
                                op -> op.uniqueBy(id).changeNoticeLogToDebug().params(paramsOp));
                    } else {
                        logger.info("Inactive Job " + id + ":" + scheduledJob.getName());
                        cron.registerNonCron(fessConfig.getSchedulerJobClassAsClass(), fessConfig.getSchedulerConcurrentExecModeAsEnum(),
                                op -> op.uniqueBy(id).changeNoticeLogToDebug().params(paramsOp));
                    }
                });
    }

    private OptionalThing<LaScheduledJob> findJobByUniqueOf(final LaJobUnique jobUnique) {
        final JobManager jobManager = ComponentUtil.getJobManager();
        try {
            return jobManager.findJobByUniqueOf(jobUnique);
        } catch (final Exception e) {
            return OptionalThing.empty();
        }
    }

    public void unregister(final ScheduledJob scheduledJob) {
        try {
            final JobManager jobManager = ComponentUtil.getJobManager();
            if (jobManager.isSchedulingDone()) {
                jobManager.findJobByUniqueOf(LaJobUnique.of(scheduledJob.getId())).ifPresent(job -> {
                    job.unschedule();
                }).orElse(() -> logger.debug("Job {} is not scheduled.", scheduledJob.getId()));
            }
        } catch (final Exception e) {
            throw new ScheduledJobException("Failed to delete Job: " + scheduledJob, e);
        }
    }

    public boolean isAvailable(final String id) {
        return ComponentUtil.getComponent(ScheduledJobBhv.class).selectByPK(id).filter(e -> Boolean.TRUE.equals(e.getAvailable()))
                .isPresent();
    }

    public void store(final JobLog jobLog) {
        ComponentUtil.getComponent(JobLogBhv.class).insertOrUpdate(jobLog, op -> {
            op.setRefresh(true);
        });
    }

}
