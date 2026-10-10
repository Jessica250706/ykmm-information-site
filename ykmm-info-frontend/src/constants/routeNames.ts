/**
 * 顶层路由名称（登录、注册、错误页等）
 */
export const CommonRouteName = {
  /** 登录 */
  LOGIN: 'Login',
  /** 注册 */
  REGISTER: 'Register',
  /** 无权限 */
  FORBIDDEN: 'Forbidden',
  /** 页面不存在 */
  NOT_FOUND: 'NotFound',
} as const

/**
 * 用户端路由名称
 */
export const UserRouteName = {
  // ---------- 卡面 ----------
  /** 卡面列表 */
  CARD_LIST: 'UserCardList',
  /** 卡面详情 */
  CARD_DETAIL: 'UserCardDetail',
  /** RC 对话浏览 */
  CARD_RC_BROWSE: 'UserCardRcBrowse',
  /** RTV 对话浏览 */
  CARD_RTV_BROWSE: 'UserCardRtvBrowse',
  /** Rabitter 对话浏览 */
  CARD_RABITTER_BROWSE: 'UserCardRabitterBrowse',

  // ---------- 剧情 ----------
  /** 剧情首页 */
  STORY: 'UserStory',
  /** 剧情浏览（仅 type） */
  STORY_BROWSE: 'UserStoryBrowse',
  /** 剧情浏览详情（type + kind + id） */
  STORY_BROWSE_DETAIL: 'UserStoryBrowseDetail',

  // ---------- 个人中心 ----------
  /** 个人中心 */
  PROFILE: 'UserProfile',
} as const

/**
 * 管理端路由名称
 */
export const AdminRouteName = {
  // ---------- 菜单管理 ----------
  MENU_MANAGE: 'AdminMenuManage',
  MENU_CREATE: 'AdminMenuCreate',
  MENU_EDIT: 'AdminMenuEdit',

  // ---------- 用户管理 ----------
  USER_MANAGE: 'AdminUserManage',

  // ---------- 人物管理 ----------
  PERSON_MANAGE: 'AdminPersonManage',
  PERSON_CREATE: 'AdminPersonCreate',
  PERSON_EDIT: 'AdminPersonEdit',

  // ---------- 角色管理 ----------
  ROLE_MANAGE: 'AdminRoleManage',
  ROLE_CREATE: 'AdminRoleCreate',
  ROLE_EDIT: 'AdminRoleEdit',

  // ---------- 卡面系列管理 ----------
  CARD_SERIES_MANAGE: 'AdminCardSeriesManage',
  CARD_SERIES_CREATE: 'AdminCardSeriesCreate',
  CARD_SERIES_EDIT: 'AdminCardSeriesEdit',

  // ---------- 卡面管理 ----------
  CARD_MANAGE: 'AdminCardManage',
  CARD_CREATE: 'AdminCardCreate',
  CARD_EDIT: 'AdminCardEdit',

  // ---------- 卡面 RC 管理 ----------
  CARD_RC_MANAGE: 'AdminCardRcManage',

  // ---------- 卡面 RTV 管理 ----------
  CARD_RTV_MANAGE: 'AdminCardRtvManage',

  // ---------- 偶像小人管理 ----------
  IDOL_MANAGE: 'AdminIdolManage',

  // ---------- 造型管理 ----------
  STYLE_MANAGE: 'AdminStyleManage',

  // ---------- 剧情管理 ----------
  STORY_MANAGE: 'AdminStoryManage',
  STORY_CREATE: 'AdminStoryCreate',
  STORY_EDIT: 'AdminStoryEdit',

  // ---------- 剧情分类管理 ----------
  STORY_CATEGORY_MANAGE: 'AdminStoryCategoryManage',
  STORY_CATEGORY_CREATE: 'AdminStoryCategoryCreate',
  STORY_CATEGORY_EDIT: 'AdminStoryCategoryEdit',
  STORY_CATEGORY_DETAIL: 'AdminStoryCategoryDetail',

  // ---------- 剧情分类类型管理 ----------
  STORY_CATEGORY_TYPE_MANAGE: 'AdminStoryCategoryTypeManage',
  STORY_CATEGORY_TYPE_EDIT: 'AdminStoryCategoryTypeEdit',

  // ---------- 表情包管理 ----------
  STICKER_MANAGE: 'AdminStickerManage',
  STICKER_CREATE: 'AdminStickerCreate',
  STICKER_EDIT: 'AdminStickerEdit',

  // ---------- 表情包分组管理 ----------
  STICKER_GROUP_MANAGE: 'AdminStickerGroupManage',
  STICKER_GROUP_CREATE: 'AdminStickerGroupCreate',
  STICKER_GROUP_EDIT: 'AdminStickerGroupEdit',

  // ---------- 贡献者管理 ----------
  CONTRIBUTOR_MANAGE: 'AdminDialogueVersionContributorManage',
  CONTRIBUTOR_CREATE: 'AdminDialogueVersionContributorCreate',
  CONTRIBUTOR_EDIT: 'AdminDialogueVersionContributorEdit',
} as const

/** 用户端路由名称联合类型 */
export type UserRouteNameType = (typeof UserRouteName)[keyof typeof UserRouteName]

/** 管理端路由名称联合类型 */
export type AdminRouteNameType = (typeof AdminRouteName)[keyof typeof AdminRouteName]

/** 顶层路由名称联合类型 */
export type CommonRouteNameType = (typeof CommonRouteName)[keyof typeof CommonRouteName]
