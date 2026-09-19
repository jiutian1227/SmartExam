/*
 Navicat MySQL Dump SQL

 Source Server         : jiutian
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : smart_exam

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 29/06/2026 17:04:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公告内容',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '公告' COMMENT '公告类型（系统通知、考试通知、公告等）',
  `target_roles` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标角色（0-管理员，1-学生，2-教师，多个角色用逗号分隔）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (1, '系统维护通知', '系统将于2026年6月15日22:00-00:00进行维护升级，期间将暂停服务。请提前保存好相关数据，感谢您的理解与支持！', '系统通知', '0,1', '2026-06-11 20:53:36', '2026-06-11 21:19:33');
INSERT INTO `announcement` VALUES (3, '期末考试安排', '期末考试即将开始，请各位同学按时参加考试。具体考试安排如下：\n\n1. 考试时间：2026年6月20日-6月25日\n2. 考试地点：各教学楼教室\n3. 注意事项：请携带学生证和考试用品\n\n祝各位同学取得好成绩！', '考试通知', '1', '2026-06-11 20:54:37', '2026-06-11 21:43:19');
INSERT INTO `announcement` VALUES (4, '教师培训通知', '本周六下午2点在行政楼301会议室将举行新教师培训，内容包括：\n\n1. 智慧考试系统使用培训\n2. 在线监考注意事项\n3. 成绩录入流程\n\n请各位教师准时参加。', '培训通知', '0', '2026-06-11 20:54:37', '2026-06-11 21:19:26');
INSERT INTO `announcement` VALUES (5, '管理员周会', '本周管理员会议将于周五上午9点在会议室召开，请各位管理员做好工作汇报准备，重点讨论：\n\n1. 系统安全检查情况\n2. 近期运维问题汇总\n3. 下学期工作计划', '会议通知', '2', '2026-06-11 20:54:37', '2026-06-11 21:43:31');
INSERT INTO `announcement` VALUES (6, '校园招聘信息', '多家知名企业将于下周来校举办招聘会，欢迎同学们踊跃参加！\n\n时间：6月18日-6月20日\n地点：大学生活动中心\n\n请提前准备好简历。', '校园通知', '1', '2026-06-11 20:54:37', '2026-06-11 20:55:07');
INSERT INTO `announcement` VALUES (9, '系统维护通知', '系统将于本周六凌晨2:00-4:00进行维护升级', 'system', '1,2', '2026-06-27 12:54:07', '2026-06-27 12:54:07');

-- ----------------------------
-- Table structure for answer_record
-- ----------------------------
DROP TABLE IF EXISTS `answer_record`;
CREATE TABLE `answer_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `exam_record_id` bigint NOT NULL COMMENT '考试记录ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `score` int NULL DEFAULT NULL COMMENT '得分',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评语',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_answer_record_exam_record`(`exam_record_id` ASC) USING BTREE,
  INDEX `fk_answer_record_question`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_answer_record_exam_record` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_record_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 138 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of answer_record
-- ----------------------------
INSERT INTO `answer_record` VALUES (122, 48, 42, 'C', 0, '回答错误，正确答案：A', '2026-06-28 18:53:30', '2026-06-29 16:48:30');
INSERT INTO `answer_record` VALUES (123, 48, 37, '@ComponentScan', 20, '学生答案完全正确，直接给出 `@ComponentScan` 作为答案，简洁明了，准确表达了 `@SpringBootApplication` 注解的全部作用。考虑到题目的难度和答案的准确性，建议给予满分。 整体来说，该答案体现了对 SpringBoot 项目基本概念的理解。', '2026-06-28 18:53:30', '2026-06-29 16:48:30');
INSERT INTO `answer_record` VALUES (124, 48, 39, '(1)核心特性： 自动配置：Spring Boot能根据类路径中的jar包依赖自动配置应用程序，减少手动配置。 起步依赖：提供预定义的依赖集合，简化Maven/Gradle配置，解决版本冲突。 嵌入式服务器：内置Tomcat、Jetty等Web服务器，应用可打包为独立可执行的JAR包运行。 生产就绪特性：提供健康检查、指标监控、配置查看等生产环境监控功能。 外部化配置：支持多层级配置文件，可根据不同环境加载不同配置。 优势： (2)快速开发：减少样板代码和XML配置，提高开发效率。 简化部署：应用可独立运行，无需外部Web服务器，部署简单。 微服务友好：轻量级启动，适合构建微服务架构。 生态丰富：提供大量Starters，集成各种常用框架。 易于测试：提供完善的测试支持，如@SpringBootTest注解。', 55, '学生答案整体水平较高，准确地描述了Spring Boot的核心特性和优势，并且使用了恰当的词汇。但部分描述不够精炼，例如“生产就绪特性”略显冗余，可以更直接地体现为“监控和管理能力”。此外，对优势的描述略显具体，可以更概括地呈现Spring Boot的价值。总的来说，答案基本符合要求，但仍有提升空间。建议给予55分。', '2026-06-28 18:53:30', '2026-06-29 16:48:30');
INSERT INTO `answer_record` VALUES (125, 49, 39, '(1)核心特性： 自动配置：Spring Boot能根据类路径中的jar包依赖自动配置应用程序，减少手动配置。 起步依赖：提供预定义的依赖集合，简化Maven/Gradle配置，解决版本冲突。 嵌入式服务器：内置Tomcat、Jetty等Web服务器，应用可打包为独立可执行的JAR包运行。 生产就绪特性：提供健康检查、指标监控、配置查看等生产环境监控功能。 外部化配置：支持多层级配置文件，可根据不同环境加载不同配置。 优势： (2)快速开发：减少样板代码和XML配置，提高开发效率。 简化部署：应用可独立运行，无需外部Web服务器，部署简单。 微服务友好：轻量级启动，适合构建微服务架构。 生态丰富：提供大量Starters，集成各种常用框架。 易于测试：提供完善的测试支持，如@SpringBootTest注解。', 22, '学生答案整体质量很高，对Spring Boot的核心思想和优势都进行了较为全面的阐述。尤其在对自动配置、起步依赖、嵌入式服务器等核心特性的描述上，抓住了重点。对优势的描述也十分到位，体现了Spring Boot在快速开发、简化部署、微服务友好等方面的优势。但由于答案略显冗长，部分表述可以精简，例如在‘生产就绪特性’和‘外部化配置’部分可以适当合并，以提高答案的简洁性。因此，建议给分22分。', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (126, 49, 40, 'Spring Boot中的Starter依赖是一组预定义的依赖集合，它将开发特定功能所需的常用库和配置捆绑在一起，简化了项目的依赖管理和配置。\n\n使用示例：若要开发一个使用Spring Data JPA进行数据库操作的Spring Boot项目，只需在pom.xml中添加spring-boot-starter-data-jpa依赖，Spring Boot会自动配置JPA相关的Bean，无需手动添加多个依赖和复杂配置。', 22, '学生答案基本准确，很好地解释了 Starter 依赖的概念和作用，并给出了一个清晰的示例。但答案略显简略，没有提及 Starter 依赖带来的其他好处，例如减少项目体积和提高可维护性。 整体来说，答案表达清晰，逻辑性强，符合题意，值得认可。 建议在回答示例时，可以更详细地说明 Spring Boot 如何自动配置 JPA Bean，例如使用了 Hibernate 等技术。', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (127, 49, 41, 'B', 10, '回答正确', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (128, 49, 64, 'B', 0, '回答错误，正确答案：A', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (129, 49, 65, 'C', 0, '回答错误，正确答案：B', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (130, 49, 53, '错误', 0, '回答错误，正确答案：正确', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (131, 49, 54, '错误', 0, '回答错误，正确答案：正确', '2026-06-28 18:54:38', '2026-06-28 20:01:00');
INSERT INTO `answer_record` VALUES (134, 54, 41, 'B', 50, '回答正确', '2026-06-28 19:49:18', '2026-06-28 19:49:49');
INSERT INTO `answer_record` VALUES (135, 54, 43, 'C', 50, '回答正确', '2026-06-28 19:49:18', '2026-06-28 19:49:49');
INSERT INTO `answer_record` VALUES (136, 55, 41, 'B', NULL, NULL, '2026-06-29 16:50:18', '2026-06-29 16:50:18');
INSERT INTO `answer_record` VALUES (137, 55, 42, 'B', NULL, NULL, '2026-06-29 16:50:18', '2026-06-29 16:50:18');

-- ----------------------------
-- Table structure for exam
-- ----------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考试标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '考试描述',
  `user_group_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration` int NOT NULL COMMENT '考试时长（分钟）',
  `total_score` int NOT NULL DEFAULT 100 COMMENT '总分',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_exam_creator`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `fk_exam_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam
-- ----------------------------
INSERT INTO `exam` VALUES (37, '期末考试模拟一', '期末考试模拟一', '1,2', '2026-06-22 16:00:00', '2026-06-29 16:00:00', 120, 100, 1, '2026-06-28 18:50:26', '2026-06-28 18:50:26');
INSERT INTO `exam` VALUES (38, '期末考试模拟二', '期末考试模拟一', '2,1', '2026-06-21 16:00:00', '2026-06-29 16:00:00', 120, 100, 1, '2026-06-28 18:51:45', '2026-06-28 18:51:45');
INSERT INTO `exam` VALUES (39, '期末考试模拟三', '期末考试模拟三', '1,2', '2026-06-27 16:00:00', '2026-07-29 16:00:00', 120, 100, 1, '2026-06-28 18:57:36', '2026-06-28 18:57:36');
INSERT INTO `exam` VALUES (40, '期末考试模拟四', '期末考试模拟四', '1,2', '2026-06-27 16:00:00', '2026-07-01 16:00:00', 120, 100, 1, '2026-06-28 19:35:12', '2026-06-28 19:35:12');
INSERT INTO `exam` VALUES (41, '测试', '测试', '1,2', '2026-06-28 16:00:00', '2026-07-30 16:00:00', 120, 100, 1, '2026-06-29 16:47:17', '2026-06-29 16:47:17');

-- ----------------------------
-- Table structure for exam_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `question_id` bigint NOT NULL COMMENT '试题ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `score` int NULL DEFAULT 10 COMMENT '该题在考试中的分值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_exam_question_exam`(`exam_id` ASC) USING BTREE,
  INDEX `fk_exam_question_question`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_exam_question_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_exam_question_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试-试题关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_question
-- ----------------------------
INSERT INTO `exam_question` VALUES (102, 37, 42, 0, '2026-06-28 18:50:34', '2026-06-28 18:50:34', 20);
INSERT INTO `exam_question` VALUES (103, 37, 37, 1, '2026-06-28 18:50:47', '2026-06-28 18:50:47', 20);
INSERT INTO `exam_question` VALUES (104, 37, 39, 2, '2026-06-28 18:50:49', '2026-06-28 18:50:49', 60);
INSERT INTO `exam_question` VALUES (105, 38, 39, 0, '2026-06-28 18:51:51', '2026-06-28 18:51:51', 25);
INSERT INTO `exam_question` VALUES (106, 38, 40, 1, '2026-06-28 18:51:52', '2026-06-28 18:51:52', 25);
INSERT INTO `exam_question` VALUES (107, 38, 41, 2, '2026-06-28 18:52:03', '2026-06-28 18:52:03', 10);
INSERT INTO `exam_question` VALUES (108, 38, 64, 3, '2026-06-28 18:52:04', '2026-06-28 18:52:04', 10);
INSERT INTO `exam_question` VALUES (109, 38, 65, 4, '2026-06-28 18:52:05', '2026-06-28 18:52:05', 10);
INSERT INTO `exam_question` VALUES (110, 38, 53, 5, '2026-06-28 18:52:12', '2026-06-28 18:52:12', 10);
INSERT INTO `exam_question` VALUES (111, 38, 54, 6, '2026-06-28 18:52:12', '2026-06-28 18:52:12', 10);
INSERT INTO `exam_question` VALUES (112, 39, 41, 0, '2026-06-28 18:57:45', '2026-06-28 18:57:45', 10);
INSERT INTO `exam_question` VALUES (113, 39, 64, 1, '2026-06-28 18:57:45', '2026-06-28 18:57:45', 10);
INSERT INTO `exam_question` VALUES (114, 39, 65, 2, '2026-06-28 18:57:45', '2026-06-28 18:57:45', 10);
INSERT INTO `exam_question` VALUES (115, 39, 51, 3, '2026-06-28 18:57:52', '2026-06-28 18:57:52', 14);
INSERT INTO `exam_question` VALUES (116, 39, 52, 4, '2026-06-28 18:57:53', '2026-06-28 18:57:53', 14);
INSERT INTO `exam_question` VALUES (117, 39, 53, 5, '2026-06-28 18:57:53', '2026-06-28 18:57:53', 14);
INSERT INTO `exam_question` VALUES (118, 39, 35, 6, '2026-06-28 18:58:03', '2026-06-28 18:58:03', 14);
INSERT INTO `exam_question` VALUES (119, 39, 36, 7, '2026-06-28 18:58:09', '2026-06-28 18:58:09', 14);
INSERT INTO `exam_question` VALUES (120, 40, 41, 0, '2026-06-28 19:35:18', '2026-06-28 19:35:18', 50);
INSERT INTO `exam_question` VALUES (121, 40, 43, 1, '2026-06-28 19:35:22', '2026-06-28 19:35:22', 50);
INSERT INTO `exam_question` VALUES (122, 41, 41, 0, '2026-06-29 16:47:25', '2026-06-29 16:47:25', 50);
INSERT INTO `exam_question` VALUES (123, 41, 42, 1, '2026-06-29 16:47:26', '2026-06-29 16:47:26', 50);

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始答题时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `score` int NULL DEFAULT NULL COMMENT '得分',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已提交，3-已批阅',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_exam_record_exam`(`exam_id` ASC) USING BTREE,
  INDEX `fk_exam_record_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_exam_record_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_exam_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_record
-- ----------------------------
INSERT INTO `exam_record` VALUES (48, 37, 2, '2026-06-28 18:52:58', '2026-06-28 18:53:30', 75, 2, '2026-06-28 18:52:57', '2026-06-29 16:48:29');
INSERT INTO `exam_record` VALUES (49, 38, 2, '2026-06-28 18:53:32', '2026-06-28 18:54:38', 54, 2, '2026-06-28 18:53:31', '2026-06-28 18:56:26');
INSERT INTO `exam_record` VALUES (54, 40, 2, '2026-06-28 19:49:00', '2026-06-28 19:49:18', 100, 2, '2026-06-28 19:48:59', '2026-06-28 19:49:48');
INSERT INTO `exam_record` VALUES (55, 41, 2, '2026-06-29 16:50:05', '2026-06-29 16:50:18', NULL, 2, '2026-06-29 16:50:05', '2026-06-29 16:50:05');

-- ----------------------------
-- Table structure for knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_point`;
CREATE TABLE `knowledge_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识点名称',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `fk_knowledge_point_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '知识点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_point
-- ----------------------------
INSERT INTO `knowledge_point` VALUES (1, 'SpringBoot', 1, '2026-05-30 15:14:20');
INSERT INTO `knowledge_point` VALUES (2, 'MyBatis', 1, '2026-05-30 15:14:33');
INSERT INTO `knowledge_point` VALUES (4, '软件测试', 1, '2026-06-06 18:55:38');
INSERT INTO `knowledge_point` VALUES (7, 'aaaa', 1, '2026-06-29 16:45:25');

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '试题ID',
  `type` tinyint NOT NULL COMMENT '题型：0-单选题，1-多选题，2-判断题，3-填空题，4-简答题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容',
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '选项（JSON格式）',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '解析',
  `knowledge_point_id` bigint NULL DEFAULT NULL COMMENT '所属知识点ID',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_question_knowledge_point`(`knowledge_point_id` ASC) USING BTREE,
  INDEX `fk_question_creator`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `fk_question_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_question_knowledge_point` FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 90 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (33, 1, '以下关于 Spring Boot 的描述，哪些是正确的？', '[\"Spring Boot 旨在完全取代 Spring Framework。\",\"自动配置是 Spring Boot 的核心特性之一。\",\"Spring Boot 强制开发者手动配置所有 Spring Bean。\",\"依赖管理是 Spring Boot 通过 Maven 或 Gradle 自动管理依赖的机制。\"]', 'A,C', 'Spring Boot 简化了 Spring 应用的搭建和配置，它会自动配置一些常用的组件。’自动配置‘ 是 Spring Boot 的核心特性之一，而 ‘依赖管理‘ 也是 Spring Boot 通过 Maven 或 Gradle 自动管理依赖的机制。', 1, 1, '2026-06-02 21:14:26', '2026-06-02 21:14:26');
INSERT INTO `question` VALUES (34, 1, '以下关于 Spring Boot Starter 项目的说法，哪些是正确的？', '[\"Spring Boot Starter 项目包含所有 Spring Boot 的依赖。\",\"Starter 项目通常用于构建大型企业级应用。\",\"它提供了预配置的依赖。\",\"它简化了 Spring Boot 应用的开发流程。\"]', 'B,C,D', 'Spring Boot Starter 项目提供了一个基于 Spring Boot 的快速启动项目，它包含了与特定功能相关的依赖项，简化了配置和开发过程。’Starter 项目‘ 提供了预配置的依赖，’它通常用于构建微服务‘  和 ‘它简化了 Spring Boot 应用的开发流程‘ 是 Starter 项目的主要优势。', 1, 1, '2026-06-02 21:14:26', '2026-06-02 21:14:26');
INSERT INTO `question` VALUES (35, 2, '在 Spring Boot 应用中，@Component 注解用于标记一个 Java 类，使其可以被 Spring 容器管理。以下哪项描述是正确的？', '', '正确', ' `@Component` 是 Spring 框架中一个通用的注解，用于标记一个可被 Spring 容器管理的对象。它告诉 Spring 容器将该类纳入其管理范围，并提供相应的依赖注入。', 1, 1, '2026-06-02 21:14:40', '2026-06-02 21:14:40');
INSERT INTO `question` VALUES (36, 2, 'Spring Boot 的自动配置功能可以自动配置 Spring 应用的某些组件，但它不能自动配置数据库连接信息。以下哪项说法正确？', '', '正确', 'Spring Boot 的自动配置主要关注于应用的通用配置，例如日志、模板、数据源等。对于数据库连接信息，Spring Boot 依赖于你手动配置或使用外部配置中心。', 1, 1, '2026-06-02 21:14:40', '2026-06-02 21:14:40');
INSERT INTO `question` VALUES (37, 3, '在 SpringBoot 项目中，`@SpringBootApplication` 注解的作用是组合了三个注解，它们分别是：`@Configuration`、`@EnableAutoConfiguration` 和 ______。', '', 'ComponentScan', ' `@ComponentScan` 注解用于扫描项目中的所有组件，并自动配置它们，是 `@SpringBootApplication` 注解的核心组成部分。', 1, 1, '2026-06-02 21:14:53', '2026-06-02 21:14:53');
INSERT INTO `question` VALUES (38, 3, '为了让 SpringBoot 应用程序能够自动配置，我们需要在项目的主类上添加注解 `@SpringBootApplication`。如果未添加此注解，SpringBoot 应用程序将无法正确启动，因为缺少 ______。', '', '自动配置', ' `@SpringBootApplication` 注解是 Spring Boot 应用程序启动的关键，它通过自动配置来简化开发过程，将一些常见的配置问题自动解决。', 1, 1, '2026-06-02 21:14:53', '2026-06-02 21:14:53');
INSERT INTO `question` VALUES (39, 4, '简述 Spring Boot 的核心思想，并说明其主要优势。', '', 'Spring Boot 的核心思想是简化 Spring 应用的开发和部署，它通过自动配置、依赖管理和快速启动等特性，极大地降低了开发者的学习成本和工作量。其主要优势在于：1. 快速启动：Spring Boot 应用程序启动速度快，无需手动配置大量 Bean。2. 自动配置：Spring Boot 会自动配置 Spring 应用，减少了开发人员的配置工作。3. 依赖管理：Spring Boot 提供了强大的依赖管理功能，可以轻松地添加和管理依赖项。4. 内嵌 Tomcat/Jetty：Spring Boot 应用程序内置了 Tomcat 或 Jetty 等 Web 服务器，简化了部署过程。', '本题考察学生对 Spring Boot 核心概念的理解，以及其与传统 Spring 项目的差异。要求学生能够清晰地阐述 Spring Boot 简化开发、提高效率的核心优势。', 1, 1, '2026-06-02 21:15:10', '2026-06-02 21:15:10');
INSERT INTO `question` VALUES (40, 4, '解释 Spring Boot 中的 ‘Starter’ 依赖，并举例说明如何使用 Starter 依赖来简化项目开发。', '', 'Spring Boot Starter 是一种预配置的依赖集合，它包含了开发特定类型的 Spring Boot 应用所需的所有依赖项。例如，`spring-boot-starter-web` 包含了开发 Web 应用所需的依赖项，如 Spring MVC、Tomcat 等。使用 Starter 依赖可以简化项目开发，因为无需手动添加和配置每个依赖项。例如，如果需要开发一个简单的 Web 应用，只需要添加 `spring-boot-starter-web` 依赖，Spring Boot 就会自动配置好 Web 应用所需的组件，开发者无需关心具体的配置细节。  通过使用 Starter，可以避免手动添加和配置大量依赖项，降低出错的风险，并提高开发效率。', '本题考察学生对 Spring Boot Starter 依赖的概念理解，以及如何利用 Starter 依赖来简化项目开发。要求学生能够举例说明 Starter 依赖的使用方法，并阐述其带来的好处。', 1, 1, '2026-06-02 21:15:10', '2026-06-02 21:15:10');
INSERT INTO `question` VALUES (41, 0, '在 Mybatis-Plus 中，使用 `MetaObject` 接口的主要作用是实现对数据库表的动态操作，以下哪一项描述最准确地体现了 `MetaObject` 的核心功能？', '[\"A. 简化了数据库连接的建立和管理，无需手动编写 JDBC 代码。\",\"B. 封装了数据库表与 Java 实体之间的映射关系，并提供了动态 SQL 构建能力。\",\"C. 自动执行数据库事务的提交和回滚操作，无需开发者手动管理。\",\"D. 提供了对数据库表的增删改查操作的直接访问，无需通过 Mapper 接口。\"]', 'B', '虽然 `MetaObject` 提供了对数据库表的便捷访问，但它真正的核心功能在于封装了数据库表与 Java 实体之间的映射关系，并提供了动态 SQL 构建的能力，从而可以根据不同的条件和逻辑灵活地生成 SQL 语句，而选项 A、C 和 D 均存在误导性，未能准确描述 `MetaObject` 的核心作用。', 2, 1, '2026-06-02 23:27:10', '2026-06-02 23:27:10');
INSERT INTO `question` VALUES (42, 0, '在 SpringBoot 项目中，@Component 注解的作用是什么？', '[\"Spring Boot 应用程序的入口类\",\"用于配置 Spring 容器的注解\",\"用于标记一个类是一个 Spring 组件\",\"用于处理 HTTP 请求的注解\"]', 'A', ' `@Component` 是 Spring 框架中一个通用的注解，用于标记一个类是一个 Spring 组件。Spring 框架会自动扫描项目中所有带有 `@Component` 注解的类，并将它们注册到 Spring 容器中。', 1, 1, '2026-06-06 00:06:29', '2026-06-06 00:06:29');
INSERT INTO `question` VALUES (43, 0, '以下哪一项最能描述软件测试的目的？', '[\"软件测试过程的执行\",\"软件测试的结果分析\",\"发现并验证软件质量\",\"软件开发人员的日常工作\"]', 'C', '软件测试旨在发现软件中的缺陷和不足，以确保软件的质量和可靠性。A选项描述的是测试过程，B选项描述的是测试结果，C选项直接点明了测试的目的。', 4, 1, '2026-06-06 18:56:11', '2026-06-06 18:56:11');
INSERT INTO `question` VALUES (44, 0, '软件测试中，以下哪一项描述了黑盒测试的核心思想？', '[\"功能测试\",\"代码审查\",\"模块测试\",\"黑盒测试\"]', 'A', '黑盒测试关注的是软件的功能和输入输出，不考虑内部结构和代码。', 4, 1, '2026-06-06 18:56:42', '2026-06-06 18:56:42');
INSERT INTO `question` VALUES (45, 0, '在软件测试过程中，‘回归测试’的主要目的是什么？', '[\"验证新功能\",\"修复缺陷\",\"验证原有功能\",\"优化代码\"]', 'B', '回归测试是为了验证在修改代码后，原有功能是否仍然正常运行。', 4, 1, '2026-06-06 18:56:42', '2026-06-06 18:56:42');
INSERT INTO `question` VALUES (46, 0, '以下哪种测试方法主要通过模拟用户行为来发现软件缺陷？', '[\"单元测试\",\"集成测试\",\"系统测试\",\"用户验收测试\"]', 'C', '用户验收测试（UAT）侧重于用户对软件的满意度和可用性。', 4, 1, '2026-06-06 18:56:42', '2026-06-06 18:56:42');
INSERT INTO `question` VALUES (47, 0, '‘测试覆盖率’指的是什么？', '[\"代码执行速度\",\"缺陷数量\",\"测试用例数量\",\"代码覆盖程度\"]', 'D', '测试覆盖率衡量的是测试活动对软件代码的覆盖程度。', 4, 1, '2026-06-06 18:56:42', '2026-06-06 18:56:42');
INSERT INTO `question` VALUES (48, 0, '以下哪一项不是软件测试的主要类型？', '[\"功能测试\",\"性能测试\",\"安全测试\",\"代码审查\"]', 'B', '代码审查是开发阶段的活动，而非测试类型。', 4, 1, '2026-06-06 18:56:42', '2026-06-06 18:56:42');
INSERT INTO `question` VALUES (49, 1, '以下关于软件测试的描述，哪些是正确的？', '[\"A. 单元测试主要关注单个模块或组件的功能正确性\",\"B. 集成测试主要关注多个模块或组件之间的交互是否正常\",\"C. 系统测试侧重于整个系统的功能和性能\",\"D. 验收测试通常由最终用户或客户进行，以验证系统是否满足其需求\",\"E. 冒烟测试主要用于确定新版本软件是否基本可用\",\"F. 性能测试只关注软件的速度。\"]', 'A,C,D', 'A. 单元测试主要关注单个模块或组件的功能正确性。B. 集成测试主要关注多个模块或组件之间的交互是否正常。C. 系统测试侧重于整个系统的功能和性能。D. 验收测试通常由最终用户或客户进行，以验证系统是否满足其需求。', 4, 1, '2026-06-06 18:57:14', '2026-06-06 18:57:14');
INSERT INTO `question` VALUES (50, 1, '下列关于测试类型的描述，哪些是正确的？', '[\"A. 白盒测试只关注软件的代码结构和逻辑\",\"B. 灰盒测试同时考虑了软件的内部结构和外部行为\",\"C. 压力测试旨在评估系统在极端负载下的性能表现\",\"D. 探索性测试依赖于预定义的测试用例\",\"E. 冒烟测试主要用于新版本软件的初步验证\",\"F. 验收测试完全由开发人员执行。\"]', 'B,C,E', 'A. 白盒测试只关注软件的代码结构和逻辑。B. 灰盒测试同时考虑了软件的内部结构和外部行为。C. 压力测试旨在评估系统在极端负载下的性能表现。D. 探索性测试依赖于预定义的测试用例。E. 冒烟测试主要用于新版本软件的初步验证。', 4, 1, '2026-06-06 18:57:14', '2026-06-06 18:57:14');
INSERT INTO `question` VALUES (51, 2, '软件测试的目的是发现软件中的缺陷，保证软件的质量。', '', '正确', '软件测试的核心目标就是发现软件缺陷，从而提高软件质量。', 4, 1, '2026-06-06 18:57:33', '2026-06-06 18:57:33');
INSERT INTO `question` VALUES (52, 2, '单元测试只关注整个软件系统的功能，不关注单个模块。', '', '错误', '单元测试的重点是验证最小的软件单元（如函数、方法）是否正确。', 4, 1, '2026-06-06 18:57:33', '2026-06-06 18:57:33');
INSERT INTO `question` VALUES (53, 2, '白盒测试主要依赖于软件的内部结构和代码。', '', '正确', '白盒测试是通过对软件代码的分析来设计测试用例。', 4, 1, '2026-06-06 18:57:33', '2026-06-06 18:57:33');
INSERT INTO `question` VALUES (54, 2, '回归测试是指在软件修改后，重新执行已通过的测试用例。', '', '正确', '回归测试确保修改后的代码没有引入新的缺陷，并且原有功能仍然正常。', 4, 1, '2026-06-06 18:57:33', '2026-06-06 18:57:33');
INSERT INTO `question` VALUES (56, 3, '软件测试的目的是发现软件中的 ______，以确保软件满足用户需求。', '', '缺陷', '本题考察了测试的目的，关键在于识别软件中的问题，而‘缺陷’是测试的核心目标。', 4, 1, '2026-06-06 18:57:54', '2026-06-06 18:57:54');
INSERT INTO `question` VALUES (57, 3, '______ 测试是一种通过模拟用户行为来验证软件功能的测试方法。', '', '功能测试', '‘功能测试’是软件测试中最常见的类型之一，其主要目标是验证软件的功能是否符合要求。', 4, 1, '2026-06-06 18:57:54', '2026-06-06 18:57:54');
INSERT INTO `question` VALUES (58, 3, '在软件测试过程中，______ 是识别和记录软件问题的重要环节。', '', '缺陷报告', '‘缺陷报告’是记录测试过程中发现的问题，是后续修复和验证的基础。', 4, 1, '2026-06-06 18:57:54', '2026-06-06 18:57:54');
INSERT INTO `question` VALUES (59, 4, '什么是软件测试的目的是什么？请用一句话概括。', '', '验证软件是否满足用户需求，并识别软件中的缺陷。', '考察学生对软件测试核心目的的理解，要求简洁明了地表达。选项内容围绕测试的目的进行设计，避免简单重复的选项。', 4, 1, '2026-06-06 18:58:13', '2026-06-06 18:58:13');
INSERT INTO `question` VALUES (60, 4, '请解释一下“黑盒测试”和“白盒测试”的区别。', '', '黑盒测试是一种不考虑软件内部结构和实现的测试方法，主要关注软件的功能和输入输出结果；而白盒测试则考虑软件内部结构和实现，测试人员需要了解代码的逻辑和流程，并以此为基础进行测试。两者测试角度不同，目标也不同。', '考察学生对两种主要测试方法的理解和区分能力。选项内容设计为常见的误解，例如混淆黑盒测试和白盒测试的测试对象。', 4, 1, '2026-06-06 18:58:13', '2026-06-06 18:58:13');
INSERT INTO `question` VALUES (61, 4, '在软件测试过程中，什么是“缺陷”？请用简单的语言描述。', '', '缺陷是指软件在实际运行过程中，表现出与需求或设计规格不符的行为，例如错误、bug、漏洞等。换句话说，缺陷是软件不符合预期的表现，需要被修复。', '考察学生对软件测试中关键概念“缺陷”的理解。选项内容设计为与缺陷相关的常见术语，但需要学生能够准确地定义和解释。', 4, 1, '2026-06-06 18:58:13', '2026-06-06 18:58:13');
INSERT INTO `question` VALUES (62, 0, '以下哪项是软件测试中用于验证系统功能是否符合需求的重要方法？', '[\"系统性能测试\",\"功能测试\",\"安全测试\",\"用户界面测试\"]', 'A', '功能测试是验证软件功能是否符合需求的主要方法。其他选项描述的是其他类型的测试。', 4, 1, '2026-06-08 11:11:05', '2026-06-08 11:11:05');
INSERT INTO `question` VALUES (63, 0, '在软件测试过程中，灰盒测试主要关注以下哪个方面？', '[\"仅关注软件的外部用户界面和功能\",\"对软件内部结构和数据流有一定了解，进行测试\",\"完全不了解软件的内部结构和数据流，仅基于需求进行测试\",\"主要用于评估软件的安全性\"]', 'B', '灰盒测试侧重于对系统内部结构和数据流的了解，进行测试。', 4, 1, '2026-06-08 11:11:05', '2026-06-08 11:11:05');
INSERT INTO `question` VALUES (64, 0, 'MyBatis 的主要作用是什么？', '[\"使用 JDBC 直接执行 SQL 语句\",\"将 Java 代码映射到 SQL 语句\",\"自动生成数据库表结构\",\"优化数据库查询性能\"]', 'A', 'MyBatis 是一种面向接口的 SQL 映射框架，它将 Java 代码与数据库操作分离，简化了数据库访问。', 2, 1, '2026-06-11 22:16:40', '2026-06-11 22:16:40');
INSERT INTO `question` VALUES (65, 0, '在 MyBatis 的 XML 映射文件中，`<mapper>` 标签的目的是什么？', '[\"定义数据库连接池配置\",\"指定 SQL 语句的执行顺序\",\"定义一个 SQL 映射器，它指定了要执行的 SQL 语句以及相应的接口\",\"管理数据库表的权限\"]', 'B', '`<mapper>` 标签用于定义一个 SQL 映射器，它指定了要执行的 SQL 语句以及相应的接口。', 2, 1, '2026-06-11 22:16:40', '2026-06-11 22:16:40');
INSERT INTO `question` VALUES (87, 0, '在 Spring Boot 项目中，@RestController 注解的作用是什么？', '[\"组合使用 @Controller 和 @ResponseBody\",\"仅用于处理 HTTP 请求\",\"仅用于返回 JSON 响应\",\"仅用于标记 RESTful API\"]', 'A', ' @RestController 是 @Controller 和 @ResponseBody 的组合，用于标记处理请求并返回响应数据的控制器。', 1, 1, '2026-06-29 07:54:33', '2026-06-29 07:54:33');
INSERT INTO `question` VALUES (88, 0, '在 Spring Boot 应用中，@RestController 注解的作用是什么？', '[\"一个控制器类处理请求。\",\"一个控制器类返回响应。\",\"一个控制器类同时处理请求和返回响应。\",\"一个控制器类用于配置 Spring Boot 应用。\"]', 'A', ' @RestController 是 @Controller 和 @ResponseBody 的组合，它用于标记一个控制器类，该类同时处理请求和返回响应。', 1, 1, '2026-06-29 15:25:31', '2026-06-29 15:25:31');
INSERT INTO `question` VALUES (89, 0, '在软件测试过程中，以下哪一项最能体现测试的价值？', '[\"减少测试时间\",\"提高测试人员的技能\",\"减少软件缺陷并提高软件质量\",\"保证软件完全没有缺陷\"]', 'C', 'A. 发现缺陷的数量 B. 测试用例的数量 C. 减少软件缺陷并提高软件质量 D. 提高测试人员的技能', 4, 1, '2026-06-29 16:46:16', '2026-06-29 16:46:16');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '角色：0-管理员，1-学生',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'teacher01', '$2a$10$8jJLyePyFVdAxyOSjimHU.8h.vDeIMavGo/ljMUEzpRkw/0fBrvtK', '李四', 0, '2026-05-26 22:59:48', '2026-06-20 22:48:28', '18888888889', 'email@email.com', '/uploads/avatars/teacher01.webp');
INSERT INTO `user` VALUES (2, 'student01', '$2a$10$H1lDOL//KeZpaUpqe.aREe/TLfZL9CtbWWbJFpExku4edUodeqZPS', '张三', 1, '2026-05-26 22:59:48', '2026-06-02 21:49:44', NULL, NULL, '/uploads/avatars/student01.webp');
INSERT INTO `user` VALUES (3, 'student02', '$2a$10$RnT1Apzh4hnnwxjF/7OMgOzRCKoIMmvxOOYOPr6C53aCYak1slAgi', '李四', 1, '2026-05-26 22:59:48', '2026-06-02 21:49:43', NULL, NULL, NULL);
INSERT INTO `user` VALUES (5, 'admin01', '$2a$10$cA8wf/UByBWi2Z70b6RskulVjDmp9VAFd.Bvkzv4ekhhczr6krkkS', '李毅', 2, '2026-05-31 16:35:14', '2026-06-18 17:59:35', NULL, 'admin@email.com', '/uploads/avatars/admin01.webp');
INSERT INTO `user` VALUES (6, 'student03', '$2a$10$RWO3HlVTkSi.9ngCwrneMeVa4gvgLnhqj49fxgRX5NiDOhxEtIvV.', '马六', 1, '2026-06-02 21:49:23', '2026-06-02 21:49:23', NULL, NULL, NULL);
INSERT INTO `user` VALUES (26, 'ceshi01', '$2a$10$VvbiKpF87ypSMnHzDyCnweTpBYa/02kzjJ3.RajlVuiIJ8wjGnyRa', '九天', 0, '2026-06-21 16:26:51', '2026-06-21 16:26:51', NULL, NULL, NULL);
INSERT INTO `user` VALUES (27, 'student99', '$2a$10$RIUbLMWYFE9nTmhpTXI3.O.tWn4Q2FfXjZwaM0OOqQYyO20ztyITC', '王五', 1, '2026-06-26 23:11:59', '2026-06-26 23:11:59', NULL, NULL, NULL);
INSERT INTO `user` VALUES (30, 'student98', '$2a$10$.LSxcZ.5Z8tYRlscimuRp.8P.Jmk4k7.ioAvmLL4clEUPX3two7nC', '马路', 1, '2026-06-28 18:32:06', '2026-06-28 18:32:06', NULL, NULL, NULL);
INSERT INTO `user` VALUES (31, 'student97', '$2a$10$XAV1Y7Lnv.0PE9jibCTsEuIBJ.YyY3m/K9mtF2VtWNcfJL4DAP8J2', '丽丽', 1, '2026-06-28 19:31:23', '2026-06-28 19:31:23', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for user_group
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户组ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户组名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户组描述',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `share_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分享码',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_user_group_creator`(`creator_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_group_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_group
-- ----------------------------
INSERT INTO `user_group` VALUES (1, '一班考试组', '班级日常考试专用分组', 1, '2026-05-26 22:59:48', '2026-06-08 11:11:26', 'd0cH3ZmV');
INSERT INTO `user_group` VALUES (2, '二班练习组', '课后练习题练习分组', 1, '2026-05-26 22:59:48', '2026-05-28 17:18:38', '2wgK6jIC');
INSERT INTO `user_group` VALUES (25, 'aaaa', 'aaaa', 1, '2026-06-29 16:48:47', '2026-06-29 16:48:47', 'eTsAFUTP');

-- ----------------------------
-- Table structure for user_group_member
-- ----------------------------
DROP TABLE IF EXISTS `user_group_member`;
CREATE TABLE `user_group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_group_id` bigint NOT NULL COMMENT '用户组ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`user_group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `fk_ugm_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_ugm_group` FOREIGN KEY (`user_group_id`) REFERENCES `user_group` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ugm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户组成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_group_member
-- ----------------------------
INSERT INTO `user_group_member` VALUES (2, 1, 3, '2026-05-26 22:59:48', '2026-05-26 22:59:48', '2026-05-26 22:59:48');
INSERT INTO `user_group_member` VALUES (10, 1, 2, '2026-05-28 17:25:04', '2026-05-28 17:25:04', '2026-05-28 17:25:04');
INSERT INTO `user_group_member` VALUES (12, 2, 2, '2026-05-30 18:18:16', '2026-05-30 18:18:16', '2026-05-30 18:18:16');

SET FOREIGN_KEY_CHECKS = 1;
