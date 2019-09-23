package ru.arlen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import ru.arlen.model.SendoutLabOrder;
import ru.arlen.model.SendoutPanelOrder;

import javax.sql.DataSource;

import java.sql.Date;
import java.sql.ResultSet;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackages = { "ru.arlen" })
@Slf4j
public class BatchConfiguration {
    //    @Override
    //    public void setDataSource(DataSource dataSource) {
    //        // override to do not set datasource even if a datasource exist.
    //        // initialize will use a Map based JobRepository (instead of database)
    //    }
    @Value("org/springframework/batch/core/schema-drop-mysql.sql")
    private Resource dropRepositoryTables;

    @Value("org/springframework/batch/core/schema-mysql.sql")
    private Resource dataRepositorySchema;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/springbatch?useSSL=false");
        ds.setUsername("gsnuser");
        ds.setPassword("lims123");
        return ds;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

        databasePopulator.addScript(dropRepositoryTables);
        databasePopulator.addScript(dataRepositorySchema);
        databasePopulator.setIgnoreFailedDrops(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator);

        return initializer;
    }

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public Job importUserJob() {
        return jobBuilderFactory.get("orderCreationJob").incrementer(new RunIdIncrementer()).start(step1()).next(step2()).build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("createLabOrder").<SendoutPanelOrder, SendoutLabOrder>chunk(2).reader(reader1())
                .processor(processor1())
                .writer(writer1())
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("updatePanelOrder").<SendoutLabOrder, SendoutPanelOrder>chunk(100).reader(reader2())
                .processor(processor2())
                .writer(writer2())
                .build();
    }

    @Bean
    public ItemStreamReader<SendoutPanelOrder> reader1() {
        JdbcCursorItemReader<SendoutPanelOrder> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT * FROM sendoutpanelorder WHERE state='NEED_ORDERING'");
        reader.setRowMapper((ResultSet rs, int i) -> {
            SendoutPanelOrder panelOrder = new SendoutPanelOrder();
            panelOrder.setId(rs.getLong("id"));
            panelOrder.setState(rs.getString("state"));
            panelOrder.setProvider(rs.getString("provider"));
            panelOrder.setPanelId(rs.getLong("panel_id"));
            panelOrder.setOrderId(rs.getLong("order_id"));
            return panelOrder;
        });
        return reader;
    }

    @Bean
    public ItemStreamReader<SendoutLabOrder> reader2() {
        JdbcCursorItemReader<SendoutLabOrder> reader2 = new JdbcCursorItemReader<>();
        reader2.setDataSource(dataSource);
        reader2.setSql("SELECT * FROM sendoutlaborder");
        reader2.setRowMapper((ResultSet rs, int i) -> {
            SendoutLabOrder labOrder = new SendoutLabOrder();
            labOrder.setOrderId(rs.getLong("order_id"));
            return labOrder;
        });
        return reader2;
    }

    @Bean
    public ItemProcessor processor1() {
        return (ItemProcessor<SendoutPanelOrder, SendoutLabOrder>) panelOrder -> {
            SendoutLabOrder labOrder = new SendoutLabOrder();
            log.info("job1 started...");
            labOrder.setOrderSent(new java.util.Date());
            labOrder.setOrderId(panelOrder.getOrderId());
            labOrder.setOrderType("orderType");
            return labOrder;
        };
    }

    @Bean
    public ItemProcessor processor2() {
        return (ItemProcessor<SendoutLabOrder, SendoutPanelOrder>) labOrder -> {
            log.info("job2 started...");
            SendoutPanelOrder panelOrder = new SendoutPanelOrder();
            panelOrder.setOrderId(labOrder.getOrderId());
            return panelOrder;
        };
    }

    @Bean
    public JdbcBatchItemWriter<SendoutLabOrder> writer1() {
        JdbcBatchItemWriter<SendoutLabOrder> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("Insert Into SendoutLabOrder(orderSent, order_id, orderType) Values (?,?,?)");
        writer.setItemPreparedStatementSetter((labOrder, ps) -> {
            ps.setDate(1, new Date(labOrder.getOrderSent().getTime()));
            ps.setLong(2, labOrder.getOrderId());
            ps.setString(3, labOrder.getOrderType());
            log.info("job1 finished...");
        });
        return writer;
    }

    @Bean
    public JdbcBatchItemWriter<SendoutPanelOrder> writer2() {
        JdbcBatchItemWriter<SendoutPanelOrder> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("Update SendoutPanelOrder Set state='AWAITIN_RESULTS' Where order_id=?");
        writer.setItemPreparedStatementSetter((panelOrder, ps) -> {
            ps.setLong(1, panelOrder.getOrderId());
            log.info("job2 finished...");
        });
        return writer;
    }
}
