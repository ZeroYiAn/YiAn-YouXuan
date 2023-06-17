package com.zero.yianyx.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zero.yianyx.model.user.Leader;
import com.zero.yianyx.model.user.User;
import com.zero.yianyx.model.user.UserDelivery;
import com.zero.yianyx.user.mapper.LeaderMapper;
import com.zero.yianyx.user.mapper.UserDeliveryMapper;
import com.zero.yianyx.user.mapper.UserMapper;
import com.zero.yianyx.user.service.UserService;
import com.zero.yianyx.vo.user.LeaderAddressVo;
import com.zero.yianyx.vo.user.UserLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description:
 * @author: ZeroYiAn
 * @time: 2023/6/12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserDeliveryMapper userDeliveryMapper;

    @Resource
    private LeaderMapper leaderMapper;

//    @Resource
//    private RegionFeignClient regionFeignClient;

    @Override
    public LeaderAddressVo getLeaderAddressVoByUserId(Long userId) {
        //根据userId查询用户默认的团长id
        LambdaQueryWrapper<UserDelivery> queryWrapper = new LambdaQueryWrapper<UserDelivery>()
                .eq(UserDelivery::getUserId,userId)
                .eq(UserDelivery::getIsDefault,1);
        UserDelivery userDelivery = userDeliveryMapper.selectOne(queryWrapper);
        if(null==userDelivery){return null;}
        //根据团长id查询团长其他信息
        Leader leader = leaderMapper.selectById(userDelivery.getLeaderId());

        LeaderAddressVo leaderAddressVo = new LeaderAddressVo();
        BeanUtils.copyProperties(leader, leaderAddressVo);

        leaderAddressVo.setUserId(userId);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());

        return leaderAddressVo;
    }

    @Override
    public User getByOpenid(String openId) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        return user;
    }

    @Override
    public UserLoginVo getUserLoginVo(Long userId) {
        UserLoginVo userLoginVo = new UserLoginVo();
        User user = this.getById(userId);
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setUserId(userId);
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setOpenId(user.getOpenId());
        userLoginVo.setIsNew(user.getIsNew());

        UserDelivery userDelivery = userDeliveryMapper.selectOne(
                new LambdaQueryWrapper<UserDelivery>()
                        .eq(UserDelivery::getUserId, userId)
                        .eq(UserDelivery::getIsDefault, 1)
        );
        if(null == userDelivery){
            userLoginVo.setLeaderId(1L);
            userLoginVo.setWareId(1L);
        }else{
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        }


        //如果是团长获取当前前团长id与对应的仓库id
//        if(user.getUserType() == UserType.LEADER) {
//            LambdaQueryWrapper<Leader> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(Leader::getUserId, userId);
//            queryWrapper.eq(Leader::getCheckStatus, 1);
//            Leader leader = leaderMapper.selectOne(queryWrapper);
//            if(null != leader) {
//                userLoginVo.setLeaderId(leader.getId());
//                Long wareId = regionFeignClient.getWareId(leader.getRegionId());
//                userLoginVo.setWareId(wareId);
//            }
//        } else {
//            //如果是会员获取当前会员对应的仓库id
//            LambdaQueryWrapper<UserDelivery> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(UserDelivery::getUserId, userId);
//            queryWrapper.eq(UserDelivery::getIsDefault, 1);
//            UserDelivery userDelivery = userDeliveryMapper.selectOne(queryWrapper);
//            if(null != userDelivery) {
//                userLoginVo.setLeaderId(userDelivery.getLeaderId());
//                userLoginVo.setWareId(userDelivery.getWareId());
//            } else {
//                userLoginVo.setLeaderId(1L);
//                userLoginVo.setWareId(1L);
//            }
//        }
        return userLoginVo;
    }
}
