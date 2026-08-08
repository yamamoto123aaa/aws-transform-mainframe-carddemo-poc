package com.carddemo.interestcalc.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mirrors INTCALC.jcl, whose single EXEC step invokes CBACT04C with the
 * business-date parameter. The whole COBOL PROCEDURE DIVISION is a single
 * sequential control-break algorithm (not a chunk-friendly
 * read/process/write pipeline, since one pass both reads TCATBAL and
 * writes back to ACCOUNT), so it is modeled as a single {@link Tasklet}
 * step rather than split across Spring Batch's ItemReader/Processor/Writer.
 */
@Configuration
public class InterestCalcJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final InterestCalcProcessor processor;

    public InterestCalcJobConfig(JobBuilderFactory jobBuilderFactory,
                                  StepBuilderFactory stepBuilderFactory,
                                  InterestCalcProcessor processor) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.processor = processor;
    }

    @Bean
    public Tasklet interestCalcTasklet(
            @Value("${interestcalc.business-date:2022071800}") String businessDate) {
        return (contribution, chunkContext) -> {
            InterestCalcResult result = processor.run(businessDate);
            contribution.incrementWriteCount(result.getTransactionsWritten());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step interestCalcStep(Tasklet interestCalcTasklet) {
        return stepBuilderFactory.get("interestCalcStep")
                .tasklet(interestCalcTasklet)
                .build();
    }

    @Bean
    public Job interestCalcJob(Step interestCalcStep) {
        return jobBuilderFactory.get("interestCalcJob")
                .start(interestCalcStep)
                .build();
    }
}
