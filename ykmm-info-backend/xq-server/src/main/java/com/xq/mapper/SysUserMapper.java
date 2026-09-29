package com.xq.mapper;

import com.xq.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    int insert(SysUser user);

    SysUser getByEmail(@Param("email") String email);

    SysUser getById(@Param("id") Long id);

    int updateById(SysUser user);

    int updateLoginInfo(@Param("id") Long id,
                        @Param("ipAddress") String ipAddress,
                        @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime);

    int updatePassword(@Param("id") Long id,
                       @Param("password") String password);
}
