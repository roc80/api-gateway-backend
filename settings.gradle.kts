rootProject.name = "api-gateway"

// 以下各个module之间完全解耦，可抽离为单独的project，为了统一管理代码，这里使用多模块
include(
    // 业务后台
    "backend",
    // 网关
    "gateway",
    // 模拟的第三方接口
    "mockapi",
    // rpc接口定义
    "api"
)