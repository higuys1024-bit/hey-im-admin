-- ----------------------------------------------------
-- 用户表增加最后登录地区字段
-- ----------------------------------------------------
ALTER TABLE `im_user` ADD COLUMN `last_login_region` VARCHAR(128) DEFAULT '' COMMENT '最后登录地区' AFTER `last_login_ip`;
