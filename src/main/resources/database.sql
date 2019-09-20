-- Table: sendoutlaborder
CREATE TABLE `sendoutpanelorder`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `state`    varchar(255) DEFAULT NULL,
    `provider` varchar(255) DEFAULT NULL,
    `panel_id` bigint(20) NOT NULL,
    `order_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = latin1;

-- Table: roles
CREATE TABLE `sendoutlaborder`
(
    `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
    `ordersent` datetime   DEFAULT NULL,
    `order_id`  bigint(20) DEFAULT NULL,
    `orderType` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = latin1;

-- Insert data

INSERT INTO sendoutpanelorder(state, provider, panel_id, order_id) VALUES ('NEED_ORDERING', 'BMGL', 20, 3);
INSERT INTO sendoutpanelorder(state, provider, panel_id, order_id) VALUES ('NEED_ORDERING', 'BMGL', 20, 3);
INSERT INTO sendoutpanelorder(state, provider, panel_id, order_id) VALUES ('NEED_ORDERING', 'BMGL', 20, 3);
INSERT INTO sendoutpanelorder(state, provider, panel_id, order_id) VALUES ('NEED_ORDERING', 'BMGL', 20, 3);
