# `com.xq.exception` 异常说明

| 异常 | 使用场景 |
|---|---|
| `BaseException` | 所有业务异常的基类，由 `GlobalExceptionHandler` 统一转成 `Result` 返回，业务代码不直接抛出。 |
| `BusinessException` | **通用业务规则校验失败**（参数不合法、状态不允许、关联对象不存在等），最常用，前端原样弹出 message。 |
| `UnauthorizedException` | 未登录 / token 无效或过期，返回 HTTP 401，前端跳登录页。 |
| `ForbiddenException` | 已登录但无权限（角色不符、非本人操作他人内容等），返回 HTTP 403。 |
| `QuizRequiredException` | 用户未通过答题被拦截，返回 HTTP 403 + body `code=423`，前端据此跳 `/quiz`。 |
| `UserNotLoginException` | 业务层取不到当前用户 ID（例如 `BaseContext.getCurrentId()` 为 null），语义与 `UnauthorizedException` 接近但更偏内部状态检查。 |
| `AccountLockedException` | 账号被锁定 / 冻结，禁止继续操作。 |
| `AccountNotFoundException` | 登录或找回密码时，账号不存在。 |
| `LoginFailedException` | 登录失败（通用提示，通常不区分"账号不存在"与"密码错误"以防账号探测）。 |
| `PasswordErrorException` | 旧密码校验不通过（如修改密码时输入的当前密码错误）。 |
| `PasswordEditFailedException` | 修改密码的整体流程失败（如写入数据库失败、不符合密码策略）。 |
| `EmailAlreadyExistsException` | 注册 / 修改邮箱时邮箱已被占用。 |
| `AlreadyExistsException` | 创建记录时与已有数据冲突（如"该组合的对话版本已存在"），比 `BusinessException` 语义更精确。 |
| `DeletionNotAllowedException` | 删除被业务规则拒绝（如"该类别下还有卡面，无法删除"）。 |
| `SerializationException` | JSON 序列化 / 反序列化失败（程序 bug 或数据损坏），属于系统异常；保留 cause 落日志，前端仅展示中性文案。 |
| `SystemException` | 系统内部错误（反射失败、枚举约定破坏、程序 bug 兜底等），完整堆栈落日志，前端返回中性提示「系统繁忙，请稍后重试」。 |

**核心原则**：业务校验一律用上述 `BaseException` 子类（首选 `BusinessException`），**不要用 `RuntimeException`**——后者会落到 handler 兜底分支，前端只会收到「未知错误」。

> `QuizRequiredException` 虽然改为继承 `BaseException`，但 `GlobalExceptionHandler` 中针对它的 `@ExceptionHandler(QuizRequiredException.class)` 分支仍然会**优先于** `BaseException` 分支命中（Spring 按最精确类型匹配），所以它仍能返回 `code=423`。