-- ----------------------------------------------------
-- 管理后台系统用户表增加 MFA 密钥字段
-- ----------------------------------------------------
ALTER TABLE `sys_user` ADD COLUMN `mfa_secret` VARCHAR(64) DEFAULT '' COMMENT 'MFA密钥(Google Authenticator)';
