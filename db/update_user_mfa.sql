-- ----------------------------------------------------
-- 管理后台系统用户表增加 MFA 密钥字段
-- ----------------------------------------------------
ALTER TABLE `sys_user` ADD COLUMN `mfa_secret` VARCHAR(64) DEFAULT '' COMMENT 'MFA密钥(Google Authenticator)';

-- 说明：
-- 1. 后端启动时 MfaUserInitRunner 会自动扫描 mfa_secret 为空的历史用户并自动生成唯一密钥落库，并在启动日志中打印。
-- 2. 如果您希望在启动前直接通过 SQL 为超级管理员 admin 预置一个已知密钥，可执行以下语句：
-- UPDATE `sys_user` SET `mfa_secret` = 'NZSGK43VORSXG5BAMJ2XI2DPOUQWY33O' WHERE `user_name` = 'admin';

