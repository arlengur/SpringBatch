package ru.arlen;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@EnableBatchProcessing
@EnableScheduling
public class MyScheduler {
    /* At each moment of time, only one instance of the task is executed */
    @Bean(name = "jbExecutorPool")
    public TaskExecutor singleThreadedJobExecutorPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100500);
        executor.setThreadNamePrefix("job-batch-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "jobLauncher")
    public JobLauncher singleThreadedJobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher sjl = new SimpleJobLauncher();
        sjl.setJobRepository(jobRepository);
        sjl.setTaskExecutor(singleThreadedJobExecutorPool());
        return sjl;
    }

    @Autowired
    @Qualifier("jobLauncher")
    private JobLauncher launcher;

    @Autowired
    Job job;

    @Scheduled(cron = "*/10 * * * * *")
    public void myScheduler() throws Exception {
        JobParameters params = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        launcher.run(job, params);
    }
}
