package com.zero.yianyx.acl.helper;

import com.zero.yianyx.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: 根据权限数据构建树型菜单数据
 * @author: ZeroYiAn
 * @time: 2023/6/8
 */

public class PermissionHelper {
    /**
     * 使用递归方式构建菜单
     * @param treeNodes  所有权限数据
     * @return 权限集合
     */
    public static List<Permission> bulid(List<Permission> treeNodes) {
        List<Permission>menuTree = new ArrayList<>();
        //treeNode 对应一项权限，如用户管理，角色管理...
        for (Permission treeNode : treeNodes) {
            //parentId=0，说明是头结点
            if(treeNode.getPid()==0){
                //权限等级设置为最高，为1，从第一层开始往下找
                treeNode.setLevel(1);
                //递归添加子节点，构造菜单树
                menuTree.add(findChildren(treeNode,treeNodes));
            }
        }
        return menuTree;
    }

    /**
     * 递归查找子节点
     * @param treeNode 父节点
     * @param treeNodes 所有节点
     * @return
     */
    private static Permission findChildren(Permission treeNode, List<Permission> treeNodes) {
        treeNode.setChildren(new ArrayList<Permission>());
        for (Permission node : treeNodes) {
            //treeNode是node的父节点
            if(treeNode.getId().longValue() == node.getPid().longValue()){
                //设置node的level为父节点level加1
                node.setLevel(treeNode.getLevel()+1);
                //为父节点添加子节点
                if(treeNode.getChildren()==null){
                    treeNode.setChildren(new ArrayList<>());
                }
                List<Permission> children = treeNode.getChildren();
                children.add(findChildren(node,treeNodes));
            }
        }
        return treeNode;

    }
}
