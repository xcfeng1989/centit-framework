package com.centit.framework.components;

import com.alibaba.fastjson.JSONObject;
import com.centit.framework.common.OptionItem;
import com.centit.framework.common.SysParametersUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.dao.ExtendedQueryPool;
import com.centit.framework.filter.HttpThreadWrapper;
import com.centit.framework.filter.RequestThreadLocal;
import com.centit.framework.model.adapter.PlatformEnvironment;
import com.centit.framework.model.basedata.*;
import com.centit.framework.security.model.CentitUserDetails;
import com.centit.support.algorithm.ListOpt;
import com.centit.support.compiler.Lexer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * cp标签实现类，并可以通过静态方法直接调用系统缓存
 *
 * @author codefan@sina.com
 * 2015-11-3
 */
public final class CodeRepositoryUtil {    
    
    public static final String LOGIN_NAME = "loginName";
    public static final String USER_CODE = "userCode";
    public static final String UNIT_CODE = "unitCode";
    public static final String DEP_NO = "depNo";
    public static final String ROLE_CODE = "roleCode";
    public static final String OPT_ID = "optId";
    public static final String OPT_CODE = "optCode";
    public static final String OPT_DESC = "optDesc";
    public static final String T = "T";
    public static final String F = "F";
    
    private CodeRepositoryUtil()
    {
        
    }
    
    private static final Logger logger = LoggerFactory.getLogger(CodeRepositoryUtil.class);

    public static final Integer MAXXZRANK = 100000;
    
    private static <T> T getCtxBean(String beanName, Class<T> clazz ) {
        WebApplicationContext ctx = ContextLoaderListener.getCurrentWebApplicationContext();
        if(ctx==null)
            return null;
        return ctx.getBean(beanName, clazz);
    }

    private static PlatformEnvironment platformEnvironment= null; 

    
    public static PlatformEnvironment getPlatformEnvironment() {
        if(platformEnvironment==null)
            platformEnvironment = getCtxBean("platformEnvironment", PlatformEnvironment.class);
        return platformEnvironment;
    }
    
    public static Map<String,? extends IUserInfo> getUserRepo() {
        return  getPlatformEnvironment().getUserRepo();
    }

    public static Map<String,? extends IUserInfo> getLoginRepo() {
        return  getPlatformEnvironment().getLoginNameRepo();
    }

      
    public static List<? extends IUnitInfo> getUnitAsTree() {
        List<? extends IUnitInfo> units = getPlatformEnvironment().listAllUnits();
        ListOpt.sortAsTree( units,( p,  c) -> StringUtils.equals(p.getUnitCode(),c.getParentUnit()) );
        return units;
    }

    public static Map<String, IUnitInfo> getUnitRepo() {
        Map<String, IUnitInfo> unitRepo = new HashMap<String, IUnitInfo>();
        List<? extends IUnitInfo> units = getPlatformEnvironment().listAllUnits();
        if(units==null)
            return unitRepo;
        for(IUnitInfo unit:units){
            unitRepo.put(unit.getUnitCode(), unit);
        }
        return unitRepo;
    }
   
    public static Map<String, IUnitInfo> getDepNo() {
        Map<String, IUnitInfo> unitRepo = new HashMap<>();
        List<? extends IUnitInfo> units = getPlatformEnvironment().listAllUnits();
        if(units==null)
            return unitRepo;
        for(IUnitInfo unit:units){
            unitRepo.put(unit.getDepNo(), unit);
        }
        return unitRepo;
    }
    
    public static Map<String,? extends IRoleInfo> getRoleRepo() {
        return getPlatformEnvironment().getRoleRepo();
    }
    
    public static Map<String,? extends IOptInfo> getOptRepo() {
        return getPlatformEnvironment().getOptInfoRepo();
    }

    /**
     * 获取操作定义（权限的控制单位）
     *
     * @return Map 操作定义（权限的控制单位）
     */
    public static Map<String,? extends IOptMethod> getPowerRepo() {
        return getPlatformEnvironment().getOptMethodRepo();
    }
    
    /**
     * 获取所有数据字典类别
     *
     * @return Map 所有数据字典类别
     */
    protected static final Map<String,String> getDataCatalogMap() {
        Map<String, String> dataCatalogMap = new HashMap<String, String>();
        List<? extends IDataCatalog> dataCatalogs = getPlatformEnvironment().listAllDataCatalogs();
        if(dataCatalogs==null)
            return dataCatalogMap;
        for(IDataCatalog dataCatalog:dataCatalogs){
            dataCatalogMap.put(dataCatalog.getCatalogCode(), dataCatalog.getCatalogName());
        }
        return dataCatalogMap;
    }
    
    /**
     * 获取数据字典
     *
     * @param sCatalog 字典类别代码
     * @return List 数据字典
     */
    public static final List<? extends IDataDictionary> getDictionary(String sCatalog) {
        return getPlatformEnvironment().listDataDictionaries(sCatalog);
    }

    public static List<? extends IUnitInfo> listAllUnits() {
        return getPlatformEnvironment().listAllUnits();
    }

    public static List<? extends IUserInfo> listAllUsers() {
        return getPlatformEnvironment().listAllUsers();
    }

    public static List<? extends IUserUnit> listAllUserUnits() {
        return getPlatformEnvironment().listAllUserUnits();
    }

    public static List<? extends IUserUnit> listUserUnits(String userCode) {
        return getPlatformEnvironment().listUserUnits(userCode);
    }
    
    public static List<? extends IUserUnit> listUnitUsers(String unitCode) {
        return getPlatformEnvironment().listUnitUsers(unitCode);
    }

    private static HttpServletRequest getLocalThreadWrapperRequest(){
        HttpThreadWrapper localThread = RequestThreadLocal.getHttpThreadWrapper();
        if(localThread!=null)
            return localThread.getRequest();
        return null;
    }
    /**
     * 获取数据字典对应的值，
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     *         userCode 用户信息 unitCode机构信息
     *         roleCode 角色信息 optId 业务信息
     * @param sKey     字典代码
     * @return  数据字典对应的值
     */
    public static final String getValue(String sCatalog, String sKey) {
        return getValue(sCatalog,sKey,WebOptUtils.getCurrentLang(getLocalThreadWrapperRequest()));
    }
    /**
     * 获取数据字典对应的值，
     *
     * @param sCatalog 字典类别代码
     * @param sKey     字典代码
     * @param localLang String类型
     * @return 数据字典对应的值
     */
    public static final String getValue(String sCatalog, String sKey,String localLang) {
        try {
            switch (sCatalog) {
                case CodeRepositoryUtil.USER_CODE:{
                    IUserInfo ui=getUserRepo().get(sKey);
                    if(ui==null)
                        return sKey;
                    return ui.getUserName();
                }
                case "userOrder":{
                    IUserInfo ui=getUserRepo().get(sKey);
                    if(ui==null)
                        return "0";
                    return ui.getUserOrder() == null ? "0" :
                        String.valueOf(ui.getUserOrder());
                }
                case CodeRepositoryUtil.LOGIN_NAME:{
                    IUserInfo ui=getLoginRepo().get(sKey);
                    if(ui==null)
                        return sKey;
                    return ui.getUserName();
                }
                case CodeRepositoryUtil.UNIT_CODE:{
                    IUnitInfo ui=getUnitRepo().get(sKey);
                    if(ui==null)
                        return sKey;
                    return ui.getUnitName();
                }
                case CodeRepositoryUtil.DEP_NO:{
                    
                    IUnitInfo ui=getUnitRepo().get(sKey);
                    if(ui==null)
                        return sKey;
                    return ui.getUnitName();
                }
                case CodeRepositoryUtil.ROLE_CODE:{
                    IRoleInfo ri= getRoleRepo().get(sKey);
                    if(ri==null)
                        return sKey;
                    return ri.getRoleName();
                }
                case CodeRepositoryUtil.OPT_ID:{
                    IOptInfo oi= getOptRepo().get(sKey);
                    if(oi==null)
                        return sKey;
                    return oi.getOptName();
                }
                case CodeRepositoryUtil.OPT_CODE:{
                    IOptMethod od= getPowerRepo().get(sKey);
                    if(od==null)
                        return sKey;
                    return od.getOptName();
                }
                case CodeRepositoryUtil.OPT_DESC:{
                    IOptMethod od = getPowerRepo().get(sKey);
                    if(od==null)
                        return sKey;
                    IOptInfo oi=  getOptRepo().get(od.getOptId());
                    if(oi==null)
                        return od.getOptName();
                    return oi.getOptName() + "-" + od.getOptName();
                }
                default:
                    IDataDictionary dictPiece = getDataPiece(sCatalog, sKey);
                    if (dictPiece == null) {
                        return sKey;
                    }
                    return dictPiece.getLocalDataValue(localLang);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return sKey;
        }
    }

    /**
     * 通过数据字典值 或者 代码
     *
     * @param sCatalog 数据字典代码
     * @param sValue   数据字典值
     * @return 部门编码映射key值
     */
    public static final String getCode(String sCatalog, String sValue) {
        if (StringUtils.isBlank(sValue)) {
            logger.info("sValue 为空中空字符串");
            return "";
        }
        try{
            switch (sCatalog) {
                case CodeRepositoryUtil.USER_CODE:
                    for (Map.Entry<String,? extends IUserInfo> ent : getUserRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getUserName()))
                            return ent.getKey();
                    }
                    return sValue;
                case CodeRepositoryUtil.LOGIN_NAME:
                    for (Map.Entry<String,? extends IUserInfo> ent : getLoginRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getUserName()))
                            return ent.getKey();
                    }
                    return sValue;
                case CodeRepositoryUtil.UNIT_CODE:
                    for (Map.Entry<String, IUnitInfo> ent : getUnitRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getUnitName()))
                            return ent.getKey();
                    }
                    return sValue;
                case CodeRepositoryUtil.DEP_NO:
                    for (Map.Entry<String,? extends IUnitInfo> ent : getUnitRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getUnitName()))
                            return ent.getValue().getDepNo();
                    }
                    return sValue;
                case CodeRepositoryUtil.ROLE_CODE:
                    for (Map.Entry<String,? extends IRoleInfo> ent : getRoleRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getRoleName()))
                            return ent.getKey();
                    }
                    return sValue;
                case CodeRepositoryUtil.OPT_ID:
                    for (Map.Entry<String,? extends IOptInfo> ent : getOptRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getOptName()))
                            return ent.getKey();
                    }
                    return sValue;
                case CodeRepositoryUtil.OPT_CODE:
                    for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                        if (sValue.equals(ent.getValue().getOptName()))
                            return ent.getKey();
                    }
                    return sValue;
                default:
                    IDataDictionary dictPiece = getDataPieceByValue(sCatalog, sValue);
                    if (dictPiece == null) {
                        return sValue;
                    }

                    return dictPiece.getDataCode();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return sValue;
        }
    }

    /**
     * 把表达式中的字典代码都 转换为 数据字典值，其他的字符 位置不变，
     *
     * @param sCatalog    数据字典代码
     * @param sExpression 表达式
     * @return 达式中的字典代码 转换为 数据字典值
     */
    public static final String transExpression(String sCatalog, String sExpression) {
        StringBuilder sb = new StringBuilder();
        Lexer lex = new Lexer();
        lex.setFormula(sExpression);

        while (true) {
            String aWord = lex.getAWord();
            if (StringUtils.isBlank(aWord)) {
                break;
            }
            aWord = getValue(sCatalog, aWord);
            sb.append(aWord);
        }

        return sb.toString();
    }

    /**
     * 获得数据字典条目的状态
     *
     * @param sCatalog 字典类别代码
     * @param sKey     字典代码
     * @return 数据字典条目的状态
     */
    public static final String getItemState(String sCatalog, String sKey) {
        try {
            if (CodeRepositoryUtil.USER_CODE.equalsIgnoreCase(sCatalog)) {
                return getUserRepo().get(sKey).getIsValid();
            }
            if (CodeRepositoryUtil.LOGIN_NAME.equalsIgnoreCase(sCatalog)) {
                return getLoginRepo().get(sKey).getIsValid();
            }
            if (CodeRepositoryUtil.UNIT_CODE.equalsIgnoreCase(sCatalog)) {
                return getUnitRepo().get(sKey).getIsValid();
            }

            if (CodeRepositoryUtil.ROLE_CODE.equalsIgnoreCase(sCatalog)) {
                return getRoleRepo().get(sKey).getIsValid();
            }

            IDataDictionary dictPiece = getDataPiece(sCatalog, sKey);
            if (dictPiece == null) {
                return "";
            }

            return dictPiece.getDataTag()==null?"N":dictPiece.getDataTag();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return sKey;
        }
    }

    /**
     * 按类别获取 业务定义信息
     *
     * @param sOptType  S:实施业务, O:普通业务, W:流程业务, I:项目业务  , M:菜单   A: 为所有
     * @return List 业务定义信息
     */
    public static final List<IOptInfo> getOptinfoList(String sOptType) {
        List<IOptInfo> optList = new ArrayList<IOptInfo>();
        for (Map.Entry<String,? extends IOptInfo> ent : getOptRepo().entrySet()) {
            IOptInfo value = ent.getValue();
            if  ( "A".equals(sOptType) || sOptType.equals(value.getOptType())
                    || ("M".equals(sOptType) && "Y".equals(value.getIsInToolbar()))) {
                optList.add(value);
            }
        }

        Collections.sort(optList, (o1,o2) -> // Long.compare(o1.getOrderInd() , o2.getOrderInd())) ;
                ( o2.getOrderInd() == null && o1.getOrderInd() == null)? 0 :
                    ( (o2.getOrderInd() == null)? 1 :
                            (( o1.getOrderInd() == null)? -1 :
                                    ((o1.getOrderInd() > o2.getOrderInd())? 1:(
                                            o1.getOrderInd() < o2.getOrderInd()?-1:0 ) ))));
        return optList;
    }

   

    /**
     * 获得一个业务下面的操作定义
     *
     * @param sOptID optId
     * @return List 一个业务下面的操作定义
     */
    public static final List<? extends IOptMethod> getOptMethodByOptID(String sOptID) {
        List<IOptMethod> optList = new ArrayList<>();
        for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
            IOptMethod value = ent.getValue();
            if (sOptID.equals(value.getOptId())) {
                optList.add(value);
            }
        }
        return optList;
    }

   
    /**
     * 获取角色信息，根据前缀获取，系统中的角色的前缀可以区分 角色的类别。
     *
     * @param sPrefix 角色的前缀
     * @return List 角色信息
     */
    public static final List<IRoleInfo> getRoleinfoList(String sPrefix) {
        List<IRoleInfo> roleList = new ArrayList<>();
        for (Map.Entry<String,? extends IRoleInfo> ent : getRoleRepo().entrySet()) {
            IRoleInfo value = ent.getValue();
            if ((value.getRoleCode().startsWith(sPrefix)  || ( value.getRoleCode().startsWith("P-")))
                    && CodeRepositoryUtil.T.equals(value.getIsValid())) {
                roleList.add(value);
            }
        }
        return roleList;
    }

    /**
     * 获取所有符合状态标记的用户，
     *
     * @param sState 用户状态， A 表示所有状态
     * @return List 所有符合状态标记的用户
     */
    public static final List<IUserInfo> getAllUsers(String sState) {
        List<? extends IUserInfo> allusers = getPlatformEnvironment().listAllUsers();
        List<IUserInfo> users = new ArrayList<>();

        if("A".equals(sState)){
            users.addAll(allusers);
            return users;
        }
       
        for (IUserInfo value : allusers) {
            if (sState.equals(value.getIsValid())) {
                users.add(value);
            }
        }       
        return users;
    }

    /**
     * 获取一个机构下面的所有以这个机构为主机构的用户，并且根据排序号排序
     *
     * @param unitCode unitCode
     * @return List 一个机构下面的所有以这个机构为主机构的用户
     */
    public static final List<IUserInfo> getSortedPrimaryUnitUsers(String unitCode) {
        List<? extends IUserUnit> unitUsers = listUnitUsers(unitCode);
        if (null == unitUsers) {
            return null;
        }
        
        List<IUserInfo> users = new ArrayList<>();
        for (IUserUnit uu :unitUsers) {
            if (!CodeRepositoryUtil.T.equals(uu.getIsPrimary())) {
                continue;
            }
            IUserInfo user = getUserRepo().get(uu.getUserCode());
            if (user != null) {
                if (CodeRepositoryUtil.T.equals(user.getIsValid())) {
                    if (!users.contains(user)) {
                        users.add(user);
                    }
                }
            }
        }

        Collections.sort(users, (o1, o2) ->
            (o1.getUserOrder() == null && o2.getUserOrder() == null) ? 0 :
                ((o1.getUserOrder() == null) ? 1 :
                    ((o2.getUserOrder() == null) ? -1 :
                        ((o1.getUserOrder() > o2.getUserOrder()) ? 1 :
                                ((o1.getUserOrder() < o2.getUserOrder()) ? -1 : 0)))));

        return users;
    }

    /**
     * 获取一个机构下面的所有用户，并且根据排序号排序
     *
     * @param unitCode unitCode
     * @return List 一个机构下面的所有用户
     */
    public static final List<IUserInfo> getSortedUnitUsers(String unitCode) {
        List<? extends IUserUnit> unitUsers = listUnitUsers(unitCode);
        if (null == unitUsers) {
            return null;
        }
 
        List<IUserInfo> users = new ArrayList<>();
        for (IUserUnit uu :unitUsers) {
            IUserInfo user = getUserRepo().get(uu.getUserCode());
            if (user != null) {
                if (CodeRepositoryUtil.T.equals(user.getIsValid())) {
                    if (!users.contains(user)) {
                        users.add(user);
                    }
                }
            }
        }

        Collections.sort(users, (o1, o2) ->
            (o1.getUserOrder() == null && o2.getUserOrder() == null) ? 0 :
                (o1.getUserOrder() == null ? 1 :
                    (o2.getUserOrder() == null ? -1 :
                        (o1.getUserOrder() > o2.getUserOrder() ? 1 :
                            (o1.getUserOrder() < o2.getUserOrder() ? -1 : 0)))));
        return users;
    }

    /**
     * 获取机构下面的所有下级机构，并且排序
     *
     * @param unitCode unitCode
     * @param unitType unitType
     * @return List 机构下面的所有下级机构
     */
    public static final List<IUnitInfo> getSortedSubUnits(String unitCode, String unitType) {
        List<IUnitInfo> units = new ArrayList<>();

        IUnitInfo ui = getUnitRepo().get(unitCode);
        for (IUnitInfo unit : ui.getSubUnits()) {
            //IUnitInfo unit = getUnitRepo().get(uu);
            if (unit != null) {
                if (CodeRepositoryUtil.T.equals(unit.getIsValid())
                        && (unitType == null || "A".equals(unitType) || unitType.indexOf(unit.getUnitType()) >= 0)) {
                    units.add(unit);
                }
            }
        }

        Collections.sort(units, (o1, o2) ->
            (o1.getUnitOrder() == null && o2.getUnitOrder() == null) ? 0 :
                (o1.getUnitOrder() == null ? 1 :
                    (o2.getUnitOrder() == null ? -1 :
                        (o1.getUnitOrder() > o2.getUnitOrder() ? 1 :
                            (o1.getUnitOrder() < o2.getUnitOrder() ? -1 :0)))));
        return units;
    }

    /**
     * 获取一个机构所有用户，没有排序
     *
     * @param unitCode unitCode
     * @return Set 一个机构所有用户
     */
    public static final Set<IUserInfo> getUnitUsers(String unitCode) {
        
        List<? extends IUserUnit> uus = listUnitUsers(unitCode);
        Set<IUserInfo> users = new HashSet<>();
        for (IUserUnit uu : uus) {
            IUserInfo user = getUserRepo().get(uu.getUserCode());
            if (user != null) {
                if (CodeRepositoryUtil.T.equals(user.getIsValid())) {
                    users.add(user);
                }
            }
        }
        return users;
    }
    
    /**
     * 获取一个用户所有机构，没有排序
     *
     * @param userCode userCode
     * @return Set 一个用户所有机构
     */
    public static final Set<IUnitInfo> getUserUnits(String userCode) {
        
        List<? extends IUserUnit> uus = listUserUnits(userCode);        
        Set<IUnitInfo> units = new HashSet<>();
        if(uus==null)
            return units;
        for (IUserUnit uu : uus) {
            IUnitInfo unit = getUnitRepo().get(uu.getUnitCode());
            if (unit != null) {
                if (CodeRepositoryUtil.T.equals(unit.getIsValid())) {
                    units.add(unit);
                }
            }
        }
        return units;
    }

    /**
     * 根据用户号获得用户信息
     *
     * @param userCode userCode
     * @return  用户信息
     */
    public static final IUserInfo getUserInfoByCode(String userCode) {
        return getUserRepo().get(userCode);
    }

    public static final IUserInfo getUserInfoByLoginName(String loginName) {
        for( IUserInfo us : getUserRepo().values()){
            if(us.getLoginName().equals(loginName))
                return us;
        }
        return null;
    }
    
    public static final IUserUnit getUserPrimaryUnit(String userCode) {
        List<? extends IUserUnit> uus = listUserUnits(userCode);   
        if(uus==null || uus.size()<1)
            return null;
        IUserUnit primaryUnit = uus.get(0);
        for (IUserUnit uu : uus) {
            if ("T".equals(uu.getIsPrimary())) {
                primaryUnit = uu;
            }
        }
        return primaryUnit;
    }
    
    private static int getXzRank(String rankCode){
        IDataDictionary dd = CodeRepositoryUtil.getDataPiece("RankType", rankCode);
        if(dd!=null)
            return Integer.valueOf(dd.getExtraCode());
        return CodeRepositoryUtil.MAXXZRANK;
    }
    /**
     * 获取用户行政角色
     *
     * @param userCode userCode
     * @param unitCode 机构代码如果是 null 系统会默认的找用户的主机构
     * @return 用户行政角色
     */
    public static final Integer getUserUnitXzRank(String userCode, String unitCode) {
        if (userCode == null) {
            return MAXXZRANK;
        }
        IUserInfo ui = getUserRepo().get(userCode);
        if (ui == null) {
            return MAXXZRANK;
        }
        String rankUnitCode = (unitCode == null) ? ui.getPrimaryUnit() : unitCode;
        if (StringUtils.isBlank(rankUnitCode)) {
            return MAXXZRANK;
        }

        List<? extends IUserUnit> uus = listUserUnits(userCode);     
        Integer nRank = MAXXZRANK;
        for (IUserUnit uu : uus) {
            if (StringUtils.equals(rankUnitCode,uu.getUnitCode()) 
                    && getXzRank(uu.getUserRank()) < nRank) {
                nRank = getXzRank(uu.getUserRank());
            }
        }
        return nRank;
    }

    /**
     * 获得已知机构 下级的所有有效机构并返回map
     *
     * @param sParentUnit sParentUnit
     * @return Map 已知机构下级的所有有效机构
     */
    public static final Map<String, IUnitInfo> getUnitMapByParaent(String sParentUnit) {
        Map<String, IUnitInfo> units = new HashMap<>();

        for (Map.Entry<String, IUnitInfo> ent : getUnitRepo().entrySet()) {
            IUnitInfo value = ent.getValue();
            if (CodeRepositoryUtil.T.equals(value.getIsValid()) && sParentUnit.equals(value.getParentUnit())) {
                units.put(ent.getKey(), value);
            }
        }
        return units;
    }

    /**
     * 根据机构代码获取机构信息
     *
     * @param sUnit sUnit 机构代码
     * @return 机构信息
     */
    public static final IUnitInfo getUnitInfoByCode(String sUnit) {
        return getUnitRepo().get(sUnit);
    }
    
    /**
     * 根据状态获取所有机构信息，
     *
     * @param sState A表示所有状态
     * @return List 所有机构信息
     */
    public static final List<IUnitInfo> getAllUnits(String sState) {
        
        List<? extends IUnitInfo> allunits = getPlatformEnvironment().listAllUnits();
        List<IUnitInfo> units = new ArrayList<>();
        if("A".equals(sState)){
            units.addAll(allunits);
            return units;
        }
       
        for (IUnitInfo unit : allunits) {
            if (sState.equals(unit.getIsValid())) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * 获得已知机构 下级的所有机构并返回map，包括失效的机构
     *
     * @param sParentUnit sParentUnit
     * @return Map 已知机构下级的所有机构，包括失效的机构
     */
    public static final Map<String, IUnitInfo> getAllUnitMapByParaent(String sParentUnit) {
        Map<String, IUnitInfo> units = new HashMap<>();

        for (Map.Entry<String, IUnitInfo> ent : getUnitRepo().entrySet()) {
            IUnitInfo value = ent.getValue();
            if (sParentUnit.equals(value.getParentUnit())) {
                units.put(ent.getKey(), value);
            }
        }
        return units;
    }

    /**
     * 获得已知机构 下级的所有有效机构并返回map，包括下级机构的下级机构
     *
     * @param sParentUnit sParentUnit
     * @return Map 已知机构下级的所有有效机构，包括下级机构的下级机构
     */
    public static final Map<String, IUnitInfo> getUnitMapBuyParaentRecurse(String sParentUnit) {
        Map<String, IUnitInfo> units = new HashMap<>();
        List<String> sParentUnits = new ArrayList<>();
        List<String> sNewUnits = new ArrayList<>();
        sParentUnits.add(sParentUnit);

        while (sParentUnits.size() > 0) {
            sNewUnits.clear();
            for (int i = 0; i < sParentUnits.size(); i++) {
                String sPNC = sParentUnits.get(i);
                for (Map.Entry<String, IUnitInfo> ent : getUnitRepo().entrySet()) {
                    IUnitInfo value = ent.getValue();
                    if (CodeRepositoryUtil.T.equals(value.getIsValid()) && sPNC.equals(value.getParentUnit())) {
                        units.put(ent.getKey(), value);
                        sNewUnits.add(ent.getKey());
                    }
                }
            }
            List<String> tempList = sParentUnits;
            sParentUnits = sNewUnits;
            sNewUnits = tempList;
        }

        return units;
    }

 
    /**
     * 获取数据字典，并整理为json机构
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     * @return 数据字典，整理为json机构
     */
    public static final String getDictionaryAsJson(String sCatalog) {
        List<? extends IDataDictionary> lsDictionary = getDictionary(sCatalog);

        List<Map<String, Object>> dataMap = new ArrayList<>();
        for (IDataDictionary dict : lsDictionary) {
            Map<String, Object> dm = new HashMap<>();
            dm.put("id", dict.getDataCode());
            dm.put("pId", dict.getExtraCode());
            dm.put("name", dict.getDataValue());
            dm.put("t", dict.getDataValue());
            dm.put("right", false);
            dataMap.add(dm);
        }
        return JSONObject.toJSONString(dataMap);
    }

   

    /**
     * 获取数据字典 ，忽略 tag 为 'D'的条目 【delete】
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     * @return 数据字典,忽略 tag 为 'D'的条目
     */
    public static final List<IDataDictionary> getDictionaryIgnoreD(String sCatalog) {
        List<IDataDictionary> dcRetMap = new ArrayList<>();
        List<? extends IDataDictionary> dcMap = getDictionary(sCatalog);
        if (dcMap != null) {
            for (IDataDictionary value : dcMap) {
                if (!"D".equals(value.getDataTag())) {// getDatatag
                    //value.setDataValue(value.getLocalDataValue(lang));
                    dcRetMap.add(value);
                }
            }
        }
        return dcRetMap;
    }

    /**
     * 获取 数据字典 key value 对
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     *         userCode 用户信息 unitCode机构信息
     *         roleCode 角色信息 optId 业务信息
     * @return Map  数据字典
     */
    public static final Map<String,String> getAllLabelValueMap(String sCatalog){
        return getAllLabelValueMap(sCatalog,WebOptUtils.getCurrentLang(
                getLocalThreadWrapperRequest()));
    }
    /**
     * 获取 数据字典 key value
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     * @param localLang localLang
     * @return Map  数据字典
     */
    public static final Map<String,String> getAllLabelValueMap(String sCatalog, String localLang) {
        Map<String,String> lbvs = new HashMap<>();

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.USER_CODE)) {
            for (Map.Entry<String,? extends IUserInfo> ent : getUserRepo().entrySet()) {
                IUserInfo value = ent.getValue();
                //if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getUserCode(),value.getUserName());
                //}
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.UNIT_CODE)) {
            for (IUnitInfo value : getUnitAsTree()) {
                //if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getUnitCode(),value.getUnitName());
                //}
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.DEP_NO)) {
            for (Map.Entry<String, IUnitInfo> ent : getDepNo().entrySet()) {
                IUnitInfo value = ent.getValue();
                // System.out.println(value.getIsvalid());
                //if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getDepNo(),value.getUnitName());
                //}
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.ROLE_CODE)) {
            for (Map.Entry<String,? extends IRoleInfo> ent : getRoleRepo().entrySet()) {
                IRoleInfo value = ent.getValue();
                //if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getRoleCode(),value.getRoleName());
                //}
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_ID)) {
            for (Map.Entry<String,? extends IOptInfo> ent : getOptRepo().entrySet()) {
                IOptInfo value = ent.getValue();
                lbvs.put(value.getOptId(),value.getOptName());
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_CODE)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod value = ent.getValue();
                lbvs.put(value.getOptCode(),value.getOptName());
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_DESC)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod optdef = ent.getValue();
                IOptInfo value = getOptRepo().get(optdef.getOptId());
                lbvs.put(optdef.getOptCode(), value.getOptName() + "-" + optdef.getOptName());
            }
            return lbvs;
        }

        List<? extends IDataDictionary> dcMap = getDictionary(sCatalog);
        if (dcMap != null) {
            for (IDataDictionary value : dcMap) {
                //if (value.getDataTag() != null && !"D".equals(value.getDataTag())) {
                lbvs.put(value.getDataCode(),value.getLocalDataValue(localLang));
                //}
            }

        }
        return lbvs;
    }

   
    /**
     * 获取 数据字典 key value 对， 忽略 禁用的 条目
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     *         userCode 用户信息 unitCode机构信息
     *         roleCode 角色信息 optId 业务信息
     * @return  数据字典,忽略禁用的条目
     */
    public static final Map<String,String> getLabelValueMap(String sCatalog){
        return getLabelValueMap(sCatalog,WebOptUtils.getCurrentLang(getLocalThreadWrapperRequest()));
    }
    /**
     * 获取 数据字典 key value 对， 忽略 禁用的 条目
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     * @param localLang localLang
     * @return 数据字典,忽略禁用的条目
     */
    public static final Map<String,String> getLabelValueMap(String sCatalog, String localLang) {
        Map<String,String> lbvs = new HashMap<String,String>();

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.USER_CODE)) {
            for (Map.Entry<String,? extends IUserInfo> ent : getUserRepo().entrySet()) {
                IUserInfo value = ent.getValue();
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getUserCode(),value.getUserName());
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.UNIT_CODE)) {
            for (IUnitInfo value : getUnitAsTree()) {
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getUnitCode(),value.getUnitName());
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.DEP_NO)) {
            for (Map.Entry<String, IUnitInfo> ent : getDepNo().entrySet()) {
                IUnitInfo value = ent.getValue();
                // System.out.println(value.getIsvalid());
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getDepNo(),value.getUnitName());
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.ROLE_CODE)) {
            for (Map.Entry<String,? extends IRoleInfo> ent : getRoleRepo().entrySet()) {
                IRoleInfo value = ent.getValue();
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.put(value.getRoleCode(),value.getRoleName());
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_ID)) {
            for (Map.Entry<String,? extends IOptInfo> ent : getOptRepo().entrySet()) {
                IOptInfo value = ent.getValue();
                lbvs.put(value.getOptId(),value.getOptName());
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_CODE)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod value = ent.getValue();
                lbvs.put(value.getOptCode(),value.getOptName());
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_DESC)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod optdef = ent.getValue();
                IOptInfo value = getOptRepo().get(optdef.getOptId());
                lbvs.put(optdef.getOptCode(), value.getOptName() + "-" + optdef.getOptName());
            }
            return lbvs;
        }

        List<? extends IDataDictionary> dcMap = getDictionary(sCatalog);
        if (dcMap != null) {
            for (IDataDictionary value : dcMap) {
                if (value.getDataTag() != null && !"D".equals(value.getDataTag())) {
                    lbvs.put(value.getDataCode(),value.getLocalDataValue(localLang));
                }
            }

        }
        return lbvs;
    }

    
    /**
     * 获取 数据字典 key value 对， 忽略 禁用的 条目
     *
     * @param sCatalog 数据字典类别，或者系统内置的类别
     * @param localLang localLang
     * @return 数据字典,忽略禁用的条目
     */
    public static final List<OptionItem> getOptionForSelect(String sCatalog, String localLang) {
         List<OptionItem> lbvs = new ArrayList<OptionItem>();

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.USER_CODE)) {
            for (Map.Entry<String,? extends IUserInfo> ent : getUserRepo().entrySet()) {
                IUserInfo value = ent.getValue();
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.add(new OptionItem(value.getUserName(),
                             value.getUserCode(), value.getPrimaryUnit()));
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.UNIT_CODE)) {
            for (IUnitInfo value : getUnitAsTree()) {
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.add(new OptionItem(value.getUnitName(),
                            value.getUnitCode(), value.getParentUnit()));
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.DEP_NO)) {
            for (Map.Entry<String, IUnitInfo> ent : getDepNo().entrySet()) {
                IUnitInfo value = ent.getValue();
                // System.out.println(value.getIsvalid());
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    IUnitInfo parentUnit = CodeRepositoryUtil.getUnitInfoByCode(value.getParentUnit());
                    lbvs.add(new OptionItem(value.getUnitName(),
                               value.getDepNo(),
                              parentUnit==null?null:parentUnit.getDepNo()));
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.ROLE_CODE)) {
            for (Map.Entry<String,? extends IRoleInfo> ent : getRoleRepo().entrySet()) {
                IRoleInfo value = ent.getValue();
                if (CodeRepositoryUtil.T.equals(value.getIsValid())) {
                    lbvs.add(new OptionItem(value.getRoleName(), value.getRoleCode()));
                }
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_ID)) {
            for (Map.Entry<String,? extends IOptInfo> ent : getOptRepo().entrySet()) {
                IOptInfo value = ent.getValue();
                lbvs.add(new OptionItem(value.getOptName(), value.getOptId()));
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_CODE)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod value = ent.getValue();
                lbvs.add(new OptionItem(value.getOptName(), value.getOptCode(),value.getOptId()));
            }
            return lbvs;
        }

        if (sCatalog.equalsIgnoreCase(CodeRepositoryUtil.OPT_DESC)) {
            for (Map.Entry<String,? extends IOptMethod> ent : getPowerRepo().entrySet()) {
                IOptMethod optdef = ent.getValue();
                IOptInfo value = getOptRepo().get(optdef.getOptId());
                lbvs.add(new OptionItem(value.getOptName() + "-" + optdef.getOptName(),
                        optdef.getOptCode(),optdef.getOptId()));
            }
            return lbvs;
        }

        List<? extends IDataDictionary> dcMap = getDictionary(sCatalog);
        if (dcMap != null) {
            for (IDataDictionary value : dcMap) {
                if (value.getDataTag() != null && !"D".equals(value.getDataTag())) {
                     lbvs.add(new OptionItem(value.getLocalDataValue(localLang) ,
                             value.getDataCode(),value.getExtraCode()));
                }
            }
        }
        return lbvs;
    }
    
    public static final List<OptionItem> getOptionForSelect(String sCatalog) {
        return getOptionForSelect(sCatalog,WebOptUtils.getCurrentLang(getLocalThreadWrapperRequest()));
    }

    /**
     * 获取字典条目
     *
     * @param sCatalog 字典类别代码
     * @param sKey     字典代码
     * @return 字典条目
     */
    public static final IDataDictionary getDataPiece(String sCatalog, String sKey) {
        
        List<? extends IDataDictionary> dcList = getDictionary(sCatalog);
        
        if (dcList == null) {
            return null;
        }

        for (IDataDictionary fd : dcList) {
            if (fd.getDataCode().equals(sKey)) {
                return fd;
            }
        }
        return null;
    }

    /**
     * 获取字典条目
     *
     * @param sCatalog 字典类别代码
     * @param sValue   字典值
     * @return 字典条目
     */
    public static final IDataDictionary getDataPieceByValue(String sCatalog, String sValue) {
        List<? extends IDataDictionary> dcList = getDictionary(sCatalog);
        if (dcList == null) {
            return null;
        }

        for (IDataDictionary fd : dcList)
            if (fd.getDataValue().equals(sValue)) {
                return fd;
            }
        return null;
    }

    /**
     * 根据机构代码获取机构名称
     *
     * @param unitCode unitCode
     * @return 机构名称
     */
    public static final String getUnitName(String unitCode) {
        IUnitInfo unitInfo = getUnitRepo().get(unitCode);
        if (unitInfo == null) {
            return "";
        }
        return unitInfo.getUnitName();
    }

    /**
     * 获取机构的下级机构
     *
     * @param allunits List allunits
     * @param parentUnitCode parentUnitCode
     * @return 机构的下级机构
     */
    private static List<IUnitInfo> fetchSubUnits(List<? extends IUnitInfo> allunits,
            String parentUnitCode) {
        if(StringUtils.isBlank(parentUnitCode) || allunits==null)
            return null;
        List<IUnitInfo> units = new ArrayList<IUnitInfo>();
        for (IUnitInfo uc : allunits) {
            if ( parentUnitCode.equals(uc.getParentUnit()) &&
                    CodeRepositoryUtil.T.equals(uc.getIsValid())) {
                        units.add(uc);

            }
        }
        return units;
    }

    /**
     * 获取机构的下级机构
     *
     * @param unitCode unitCode
     * @return 机构的下级机构
     */
    public static final List<IUnitInfo> getSubUnits(String unitCode) {

        List<? extends IUnitInfo> units = getPlatformEnvironment().listAllUnits();

        return fetchSubUnits(units,unitCode);
    }
    /**
     * 获取机构的下级机构，并按照树形排列
     *
     * @param unitCode unitCode
     * @return 机构的下级机构,并按照树形排列
     */
    public static final List<IUnitInfo> getAllSubUnits(String unitCode) {
        if(StringUtils.isBlank(unitCode))
            return null;
        List<? extends IUnitInfo> allunits = getPlatformEnvironment().listAllUnits();

        List<IUnitInfo> units = new ArrayList<IUnitInfo>();
        List<IUnitInfo> subunits = fetchSubUnits(allunits,unitCode);
        while( subunits!=null && subunits.size()>0){
            units.addAll(subunits);
            List<IUnitInfo> subunits1 = new ArrayList<IUnitInfo>();
            for(IUnitInfo u1: subunits){
                List<IUnitInfo> subunits2 = fetchSubUnits(allunits,u1.getUnitCode());
                if(subunits2!=null)
                    subunits1.addAll(subunits2);
            }
            subunits = subunits1;
        }

        ListOpt.sortAsTree(units, (p,c) -> StringUtils.equals(p.getUnitCode(),c.getParentUnit()));
        return units;     
    }

    /**
     * 获取所有机构信息，并返回json格式。
     *
     * @return 所有机构信息，json格式
     */
    public static final String getUnitsJson() {

        List<Map<String, Object>> dataMap = new ArrayList<>();
        for (Map.Entry<String, IUnitInfo> ent : getUnitRepo().entrySet()) {
            Map<String, Object> dm = new HashMap<>();
            IUnitInfo u = ent.getValue();
            JSONObject rs = new JSONObject();
            dm.put("MID", u.getUnitCode());
            dm.put("MText", u.getUnitName());
            dm.put("ParentID", u.getParentUnit());
            dataMap.add(rs);
        }

        Map<String, Object> result = new HashMap<>();

        result.put("menuList", dataMap);
        return JSONObject.toJSONString(result);
    }

    /**
     * 验证当前用户是否有某个操作方法的权限
     *
     * @param optId optId
     * @param IOptMethod IOptMethod
     * @return 操作方法的权限
     */
    public static final Boolean checkUserOptPower(String optId, String IOptMethod) {
        CentitUserDetails userDetails = (CentitUserDetails) WebOptUtils.getLoginUser(
                getLocalThreadWrapperRequest());
        if (null == userDetails) {
            return false;
        }

//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        CentitUserDetailsImpl userDetails = (CentitUserDetailsImpl) principal;
        return userDetails.checkOptPower(optId, IOptMethod);
    }
    
    /**
     * 获取用户所有的 操作方法
     * @return 返回一个map，key为optid+‘-’+method value 为 'T'
     */
    public static final Map<String,String> getUserAllOptPowers() {
        CentitUserDetails userDetails = (CentitUserDetails)
                WebOptUtils.getLoginUser(getLocalThreadWrapperRequest());
        if (null == userDetails) {
            return null;
        }
        return userDetails.getUserOptList();
    }

    /**
     * 获取用户参数设置
     *
     * @param paramCode 参数代码
     * @return 用户参数设置
     */
    public static final String getUserSettingValue(String paramCode) {
        CentitUserDetails userDetails = (CentitUserDetails) WebOptUtils.getLoginUser(
                getLocalThreadWrapperRequest());
        if (null == userDetails) {
            return null;
        }

        return userDetails.getUserSettingValue(paramCode);
    }
    
    /**
     * 获取用户所有参数设置
     * @return 返回 key - value 对（map）
     */
    public static final Map<String, String> getUserAllSettings() {
        CentitUserDetails userDetails = (CentitUserDetails) WebOptUtils.getLoginUser(
                getLocalThreadWrapperRequest());
        if (null == userDetails) {
            return null;
        }
        return userDetails.getUserSettings();
    }
    /**
     * 获取System.properties文件属性值
     * @param key 配置信息key值
     * @return System.properties文件属性值
     */
    public static final String getSysConfigValue(String key) {
        return SysParametersUtils.getStringValue(key);
    }


    
    public static final String getExtendedSql(String extendedSqlId){
           return ExtendedQueryPool.getExtendedSql(extendedSqlId);
    }
    
}
