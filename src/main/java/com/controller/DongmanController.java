
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 国漫先驱
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/dongman")
public class DongmanController {
    private static final Logger logger = LoggerFactory.getLogger(DongmanController.class);

    private static final String TABLE_NAME = "dongman";

    @Autowired
    private DongmanService dongmanService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DongmanCollectionService dongmanCollectionService;
    //级联表非注册的service
    //注册表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("dongmanDeleteStart",1);params.put("dongmanDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = dongmanService.queryPage(params);

        //字典表数据转换
        List<DongmanView> list =(List<DongmanView>)page.getList();
        for(DongmanView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        DongmanEntity dongman = dongmanService.selectById(id);
        if(dongman !=null){
            //entity转view
            DongmanView view = new DongmanView();
            BeanUtils.copyProperties( dongman , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody DongmanEntity dongman, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,dongman:{}",this.getClass().getName(),dongman.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<DongmanEntity> queryWrapper = new EntityWrapper<DongmanEntity>()
            .eq("dongman_name", dongman.getDongmanName())
            .eq("dongman_types", dongman.getDongmanTypes())
            .eq("dongman_video", dongman.getDongmanVideo())
            .eq("dongman_shijian", dongman.getDongmanShijian())
            .eq("dongman_faxing", dongman.getDongmanFaxing())
            .eq("dongman_add", dongman.getDongmanAdd())
            .eq("dongman_gs", dongman.getDongmanGs())
            .eq("chuchang_time", new SimpleDateFormat("yyyy-MM-dd").format(dongman.getChuchangTime()))
            .eq("dongman_sc", dongman.getDongmanSc())
            .eq("dongman_jishu", dongman.getDongmanJishu())
            .eq("dongman_daoyan", dongman.getDongmanDaoyan())
            .eq("zhuangtai_types", dongman.getZhuangtaiTypes())
            .eq("dongman_delete", dongman.getDongmanDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        DongmanEntity dongmanEntity = dongmanService.selectOne(queryWrapper);
        if(dongmanEntity==null){
            dongman.setDongmanDelete(1);
            dongman.setInsertTime(new Date());
            dongman.setCreateTime(new Date());
            dongmanService.insert(dongman);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody DongmanEntity dongman, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,dongman:{}",this.getClass().getName(),dongman.toString());
        DongmanEntity oldDongmanEntity = dongmanService.selectById(dongman.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<DongmanEntity> queryWrapper = new EntityWrapper<DongmanEntity>()
            .notIn("id",dongman.getId())
            .andNew()
            .eq("dongman_name", dongman.getDongmanName())
            .eq("dongman_types", dongman.getDongmanTypes())
            .eq("dongman_video", dongman.getDongmanVideo())
            .eq("dongman_shijian", dongman.getDongmanShijian())
            .eq("dongman_faxing", dongman.getDongmanFaxing())
            .eq("dongman_add", dongman.getDongmanAdd())
            .eq("dongman_gs", dongman.getDongmanGs())
            .eq("chuchang_time", new SimpleDateFormat("yyyy-MM-dd").format(dongman.getChuchangTime()))
            .eq("dongman_sc", dongman.getDongmanSc())
            .eq("dongman_jishu", dongman.getDongmanJishu())
            .eq("dongman_daoyan", dongman.getDongmanDaoyan())
            .eq("zhuangtai_types", dongman.getZhuangtaiTypes())
            .eq("dongman_delete", dongman.getDongmanDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        DongmanEntity dongmanEntity = dongmanService.selectOne(queryWrapper);
        if("".equals(dongman.getDongmanPhoto()) || "null".equals(dongman.getDongmanPhoto())){
                dongman.setDongmanPhoto(null);
        }
        if("".equals(dongman.getDongmanVideo()) || "null".equals(dongman.getDongmanVideo())){
                dongman.setDongmanVideo(null);
        }
        if(dongmanEntity==null){
            dongmanService.updateById(dongman);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<DongmanEntity> oldDongmanList =dongmanService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<DongmanEntity> list = new ArrayList<>();
        for(Integer id:ids){
            DongmanEntity dongmanEntity = new DongmanEntity();
            dongmanEntity.setId(id);
            dongmanEntity.setDongmanDelete(2);
            list.add(dongmanEntity);
        }
        if(list != null && list.size() >0){
            dongmanService.updateBatchById(list);
        }

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<DongmanEntity> dongmanList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            DongmanEntity dongmanEntity = new DongmanEntity();
//                            dongmanEntity.setDongmanName(data.get(0));                    //动漫名称 要改的
//                            dongmanEntity.setDongmanTypes(Integer.valueOf(data.get(0)));   //国漫先驱类型 要改的
//                            dongmanEntity.setDongmanPhoto("");//详情和图片
//                            dongmanEntity.setDongmanVideo(data.get(0));                    //介绍视频 要改的
//                            dongmanEntity.setDongmanShijian(data.get(0));                    //更新时间 要改的
//                            dongmanEntity.setDongmanFaxing(data.get(0));                    //出品公司 要改的
//                            dongmanEntity.setDongmanAdd(data.get(0));                    //地    区 要改的
//                            dongmanEntity.setDongmanGs(data.get(0));                    //发行公司 要改的
//                            dongmanEntity.setChuchangTime(sdf.parse(data.get(0)));          //发行日期 要改的
//                            dongmanEntity.setDongmanSc(data.get(0));                    //单集时长 要改的
//                            dongmanEntity.setDongmanJishu(data.get(0));                    //集    数 要改的
//                            dongmanEntity.setDongmanDaoyan(data.get(0));                    //导    演 要改的
//                            dongmanEntity.setZhuangtaiTypes(Integer.valueOf(data.get(0)));   //动漫状态 要改的
//                            dongmanEntity.setDongmanContent("");//详情和图片
//                            dongmanEntity.setDongmanDelete(1);//逻辑删除字段
//                            dongmanEntity.setInsertTime(date);//时间
//                            dongmanEntity.setCreateTime(date);//时间
                            dongmanList.add(dongmanEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        dongmanService.insertBatch(dongmanList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 个性推荐
    */
    @IgnoreAuth
    @RequestMapping("/gexingtuijian")
    public R gexingtuijian(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("gexingtuijian方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        CommonUtil.checkMap(params);
        List<DongmanView> returnDongmanViewList = new ArrayList<>();

        //查看收藏
        Map<String, Object> params1 = new HashMap<>(params);params1.put("sort","id");params1.put("yonghuId",request.getSession().getAttribute("userId"));
        PageUtils pageUtils = dongmanCollectionService.queryPage(params1);
        List<DongmanCollectionView> collectionViewsList =(List<DongmanCollectionView>)pageUtils.getList();
        Map<Integer,Integer> typeMap=new HashMap<>();//购买的类型list
        for(DongmanCollectionView collectionView:collectionViewsList){
            Integer dongmanTypes = collectionView.getDongmanTypes();
            if(typeMap.containsKey(dongmanTypes)){
                typeMap.put(dongmanTypes,typeMap.get(dongmanTypes)+1);
            }else{
                typeMap.put(dongmanTypes,1);
            }
        }
        List<Integer> typeList = new ArrayList<>();//排序后的有序的类型 按最多到最少
        typeMap.entrySet().stream().sorted((o1, o2) -> o2.getValue() - o1.getValue()).forEach(e -> typeList.add(e.getKey()));//排序
        Integer limit = Integer.valueOf(String.valueOf(params.get("limit")));
        for(Integer type:typeList){
            Map<String, Object> params2 = new HashMap<>(params);params2.put("dongmanTypes",type);
            PageUtils pageUtils1 = dongmanService.queryPage(params2);
            List<DongmanView> dongmanViewList =(List<DongmanView>)pageUtils1.getList();
            returnDongmanViewList.addAll(dongmanViewList);
            if(returnDongmanViewList.size()>= limit) break;//返回的推荐数量大于要的数量 跳出循环
        }
        //正常查询出来商品,用于补全推荐缺少的数据
        PageUtils page = dongmanService.queryPage(params);
        if(returnDongmanViewList.size()<limit){//返回数量还是小于要求数量
            int toAddNum = limit - returnDongmanViewList.size();//要添加的数量
            List<DongmanView> dongmanViewList =(List<DongmanView>)page.getList();
            for(DongmanView dongmanView:dongmanViewList){
                Boolean addFlag = true;
                for(DongmanView returnDongmanView:returnDongmanViewList){
                    if(returnDongmanView.getId().intValue() ==dongmanView.getId().intValue()) addFlag=false;//返回的数据中已存在此商品
                }
                if(addFlag){
                    toAddNum=toAddNum-1;
                    returnDongmanViewList.add(dongmanView);
                    if(toAddNum==0) break;//够数量了
                }
            }
        }else {
            returnDongmanViewList = returnDongmanViewList.subList(0, limit);
        }

        for(DongmanView c:returnDongmanViewList)
            dictionaryService.dictionaryConvert(c, request);
        page.setList(returnDongmanViewList);
        return R.ok().put("data", page);
    }

    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = dongmanService.queryPage(params);

        //字典表数据转换
        List<DongmanView> list =(List<DongmanView>)page.getList();
        for(DongmanView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        DongmanEntity dongman = dongmanService.selectById(id);
            if(dongman !=null){


                //entity转view
                DongmanView view = new DongmanView();
                BeanUtils.copyProperties( dongman , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody DongmanEntity dongman, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,dongman:{}",this.getClass().getName(),dongman.toString());
        Wrapper<DongmanEntity> queryWrapper = new EntityWrapper<DongmanEntity>()
            .eq("dongman_name", dongman.getDongmanName())
            .eq("dongman_types", dongman.getDongmanTypes())
            .eq("dongman_video", dongman.getDongmanVideo())
            .eq("dongman_shijian", dongman.getDongmanShijian())
            .eq("dongman_faxing", dongman.getDongmanFaxing())
            .eq("dongman_add", dongman.getDongmanAdd())
            .eq("dongman_gs", dongman.getDongmanGs())
            .eq("dongman_sc", dongman.getDongmanSc())
            .eq("dongman_jishu", dongman.getDongmanJishu())
            .eq("dongman_daoyan", dongman.getDongmanDaoyan())
            .eq("zhuangtai_types", dongman.getZhuangtaiTypes())
            .eq("dongman_delete", dongman.getDongmanDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        DongmanEntity dongmanEntity = dongmanService.selectOne(queryWrapper);
        if(dongmanEntity==null){
            dongman.setDongmanDelete(1);
            dongman.setInsertTime(new Date());
            dongman.setCreateTime(new Date());
        dongmanService.insert(dongman);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}
