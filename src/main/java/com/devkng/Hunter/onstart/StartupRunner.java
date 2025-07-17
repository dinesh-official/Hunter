package com.devkng.Hunter.onstart;

import com.clickhouse.logging.Logger;
import com.clickhouse.logging.LoggerFactory;
import com.devkng.Hunter.scheduler.MyScheduler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);
    private final MyScheduler myScheduler;

    public StartupRunner(MyScheduler myScheduler, DatabaseInitializer databaseInitializer) {
        this.myScheduler = myScheduler;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Application started. Triggering first SSH check manually.");
       // myScheduler.executeSshCheck(); // clean method call, avoids scheduler annotation problems
        logger.info("SSH check");
    }

}
