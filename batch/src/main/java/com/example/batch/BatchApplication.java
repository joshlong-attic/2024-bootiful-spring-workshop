package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    FlatFileItemReader<Customer> flatFileItemReader(@Value("classpath:/input.csv") Resource resource) {
        return new FlatFileItemReaderBuilder<Customer>()
//                .lineTokenizer(new DelimitedLineTokenizer(","))
                .resource(resource)
                .name("csvFlatFileItemReader")
                .linesToSkip(1)
                .delimited().names("id", "name") 
                .fieldSetMapper(fieldSet -> new Customer(fieldSet.readInt( "id"), fieldSet.readString("name")))
                .build();
    }

    @Bean
    JdbcBatchItemWriter<Customer> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .assertUpdates(true)
                .dataSource(dataSource)
                .sql("insert into customer (id,name) values (?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id()); 
                    ps.setString(2, item.name()); 
                })
                .build();
    }

    @Bean
    Step start(JobRepository repository, PlatformTransactionManager transactionManager,
               FlatFileItemReader<Customer> reader, JdbcBatchItemWriter<Customer> writer) {
        return new StepBuilder("startStep", repository)
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    Job job(JobRepository repository, Step start) {
        return new JobBuilder("csvToDbJob", repository)
                .start(start)
                .build();
    }

}

record Customer(int id, String name) {
}