/**
 *    Filename : ExpUtil.java
 *    Author   : shen愛
 *    Date     : 2014年4月25日
 *
 * Copyright(c) 2011-2013 Mobitide Android Team. All Rights Reserved.
 */
package com.cx.thunder.util;

/**
 * @author shen愛
 *
 */
public class ExpUtil {

    /**
     * 经验值基数。200为1级
     */
    static final int EXP_BASIC=200;
    
    /**
     * 经验值累加值，每升一级，在原有的技术上加100
     */
    static final int EXP_ADD=100;
    
    /**
     * 根据经验值获取等级
     * @param exp
     * @return 等级
     */
    public static int getLevel(int exp){
        
        int level=0;
        
        while(exp>0){
            //基数为EXP_BASIC,每增加一级，累加EXP_ADD
            exp-=(level*EXP_ADD+EXP_BASIC);
            level++;
        }
        
        return level-1;
        
    }
}
