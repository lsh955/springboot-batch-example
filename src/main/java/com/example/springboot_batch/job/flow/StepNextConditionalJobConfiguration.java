package com.example.springboot_batch.job.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 이승환
 * @since 2019/12/31
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    /**
     * code scenario
     * step1 실패 시나리오: step1 -> step3
     * step1 성공 시나리오: step1 -> step2 -> step3
     */


    @Bean
    public Job stepNextConditionalJob() {
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalJobStep1())   // conditionalJobStep1 을 실행한다.
                    .on("FAILED")        // FAILED 일 경우
                    .to(conditionalJobStep3())  // step3 으로 이동한다.
                    .on("*")             // step3의 결과 관계 없이
                    .end()                      // step3으로 이동하면 Flow가 종료된다.
                .from(conditionalJobStep1())    // step1 로 부터
                    .on("*")             // FAILED 외에 모든경우
                    .to(conditionalJobStep2())  // step2 로 이동한다.
                    .next(conditionalJobStep3())// step2 가 정상 종료되면 step3으로 이동한다.
                    .on("*")             // step3의 결과 관계 없이
                    .end()                      // step3으로 이동하면 Flow가 종료한다.
                .end()                          // Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {

                    log.info(">>>>> This is stepNextConditionalJob Step1");

                    /**
                     * ExitStatus를 FAILED로 지정한다.
                     * 해당 status를 보고 flow가 진행된다.
                     */
                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep2() {
        return stepBuilderFactory.get("conditionalJobStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalJobStep3() {
        return stepBuilderFactory.get("conditionalJobStep3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
