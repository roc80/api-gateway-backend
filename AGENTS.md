# Agent 工作规范（本文件会被 OpenHands 读取并遵守）

## 技术栈与约定
- 语言/框架: Java21 / SpringBoot3.4.1
- 包管理: gradle

## 注意事项
- 仔细阅读README.md后，理解项目当前现状后再尝试运行项目。

## 红线（任何时候禁止）
- 禁止删除数据库/执行 DROP / TRUNCATE
- 禁止 rm -rf / git push --force 到主干
- 禁止改动 .env / 密钥文件 / CI 配置除非任务明确要求
- 改动后必须保证 TEST_CMD 通过

## 交付要求
- 只改与任务相关的文件
- 改完必须运行测试, 把结果写进最终回复
- 不确定时优先问, 不要乱猜 API
