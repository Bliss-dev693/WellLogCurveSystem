import request from '@/utils/request'

// 用户注册 - 使用 x-www-form-urlencoded 格式
export const userRegisterService = ({ username, password }) => 
  request.post('/user/register', 
    new URLSearchParams({ username, password }).toString(),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  )

// 用户登录 - 使用 x-www-form-urlencoded 格式
export const userLoginService = ({ username, password }) =>
  request.post('/user/login', 
    new URLSearchParams({ username, password }).toString(),
    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  )

// 获取用户信息
export const userGetInfoService = () => request.get('/user/userInfo')

// 更新用户信息
export const userUpdateInfoService = (data) => request.put('/user/update', data)

// 更新用户头像 - 根据接口文档修改
export const userUpdateAvatarService = (avatarurl) => 
  request.patch('/user/updateAvatar', null, {
    params: { avatarurl }
  })

// 更新用户密码 - 根据接口文档修改路径和参数
export const userUpdatePasswordService = ({ old_pwd, new_pwd, re_pwd }) =>
  request.patch('/user/updatePwd', 
    // 修正：直接传递JSON对象，无需转为URLSearchParams（request会自动处理JSON格式）
    { old_pwd, new_pwd, re_pwd },
    { 
      headers: { 
        'Content-Type': 'application/json;charset=UTF-8' // 明确指定JSON格式（若request全局已配置可省略）
      } 
    }
  )
