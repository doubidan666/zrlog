package com.zrlog.model;

import com.hibegin.common.util.StringUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.zrlog.common.rest.request.PageRequest;
import com.zrlog.data.dto.PageData;
import com.zrlog.util.ParseUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 存放文章数据，对应数据的log表。
 */
public class Log extends Model<Log> implements Serializable {

    public static final String TABLE_NAME = "log";
    private boolean privacy;
    private boolean rubbish;

    public Log(boolean privacy, boolean rubbish) {
        this.rubbish = rubbish;
        this.privacy = privacy;
    }

    public Log() {
    }

    public Log findByIdOrAlias(Object idOrAlias) {
        if (idOrAlias == null) {
            return null;
        }
        if (idOrAlias instanceof Integer) {
            String sql = "select l.*,last_update_date as lastUpdateDate,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize ,t.alias as typeAlias,t.typeName as typeName  from " + TABLE_NAME + " l inner join user u,type t where t.typeId=l.typeId and u.userId=l.userId and rubbish=? and privacy=? and l.logId=?";
            Log log = findFirst(sql, rubbish, privacy, idOrAlias);
            if (log != null) {
                return log;
            }
        }
        String sql = "select l.*,last_update_date as lastUpdateDate,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize ,t.alias as typeAlias,t.typeName as typeName  from " + TABLE_NAME + " l inner join user u,type t where t.typeId=l.typeId and u.userId=l.userId and rubbish=? and privacy=? and l.alias=?";
        return findFirst(sql, rubbish, privacy, idOrAlias);
    }

    /**
     * 这个用于Admin 进行查询不检查
     *
     * @param idOrAlias
     * @return
     */
    public Log adminFindByIdOrAlias(Object idOrAlias) {
        if (idOrAlias == null) {
            return null;
        }
        if (idOrAlias instanceof Integer) {
            String sql = "select l.*,last_update_date as lastUpdateDate,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize ,t.alias as typeAlias,t.typeName as typeName  from " + TABLE_NAME + " l inner join user u,type t where t.typeId=l.typeId and u.userId=l.userId and l.logId=?";
            Log log = findFirst(sql, idOrAlias);
            if (log != null) {
                return log;
            }
        }
        String sql = "select l.*,last_update_date as lastUpdateDate,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize ,t.alias as typeAlias,t.typeName as typeName  from " + TABLE_NAME + " l inner join user u,type t where t.typeId=l.typeId and u.userId=l.userId and l.alias=?";
        return findFirst(sql, idOrAlias);
    }

    public Log findLastLog(int id, String notFoundDesc) {
        String lastLogSql = "select l.alias as alias,l.title as title from " + TABLE_NAME + " l where rubbish=? and privacy=? and l.logId<? order by logId desc";
        Log log = findFirst(lastLogSql, rubbish, privacy, id);
        if (log == null) {
            log = new Log().set("alias", id).set("title", notFoundDesc);
        }
        return log;
    }

    public Log findNextLog(int id, String notFoundDesc) {
        String nextLogSql = "select l.alias as alias,l.title as title from " + TABLE_NAME + " l where rubbish=? and privacy=? and l.logId>?";
        Log log = findFirst(nextLogSql, rubbish, privacy, id);
        if (log == null) {
            log = new Log().set("alias", id).set("title", notFoundDesc);
        }
        return log;
    }

    public int findMaxId() {
        Log log = findFirst("select max(logId) max from " + TABLE_NAME);
        if (log.getInt("max") != null) {
            return log.getInt("max");
        }
        return 0;
    }

    public PageData<Log> find(PageRequest pageRequest) {
        PageData<Log> data = new PageData<>();
        String sql = "select l.*,t.typeName,t.alias as typeAlias,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize from " + TABLE_NAME + " l inner join user u inner join type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeid=l.typeid  order by l.logId desc limit  ?,?";

        data.setRows(find(sql, rubbish, privacy, ParseUtil.getFirstRecord(pageRequest.getPage(), pageRequest.getSize()), pageRequest.getSize()));
        ModelUtil.fillPageData(this, "from " + TABLE_NAME + " l inner join user u where rubbish=? and privacy=? and u.userId=l.userId ", data,
                new Object[]{rubbish, privacy});
        return data;
    }

    /**
     * 管理员查询文章
     *
     * @param page
     * @param pageSize
     * @param keywords
     * @param order
     * @param field
     * @return
     */
    public PageData<Log> find(int page, int pageSize, String keywords, String order, String field) {
        PageData<Log> data = new PageData<>();
        String searchKeywords = "";
        List<Object> searchParam = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotEmpty(keywords)) {
            searchKeywords = " and (l.title like ? or l.plain_content like ? or l.keywords like ?)";
            searchParam.add("%" + keywords + "%");
            searchParam.add("%" + keywords + "%");
            searchParam.add("%" + keywords + "%");
            params.addAll(searchParam);
        }
        String pageSort = "l.logId desc";
        String sortField = field;
        if (order != null && !"".equals(order) && field != null && !"".equals(field)) {
            if ("id".equals(sortField)) {
                sortField = "logId";
            } else if ("typeName".equals(sortField)) {
                sortField = "typeId";
            } else if ("privacy".equals(sortField)) {
                sortField = "privacy";
            } else if ("lastUpdateDate".equals(sortField)) {
                sortField = "last_update_date";
            } else {
                sortField = "logId";
            }
            pageSort = "l." + sortField + " " + order;
        }
        params.add(ParseUtil.getFirstRecord(page, pageSize));
        params.add(pageSize);
        String sql = "select l.*,l.privacy privacy,t.typeName,l.logId as id,l.last_update_date as lastUpdateDate,t.alias as typeAlias,u.userName,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId ) commentSize from " + TABLE_NAME + " l inner join user u inner join type t where u.userId=l.userId" + searchKeywords + " and t.typeid=l.typeid order by " + pageSort + " limit ?,?";
        data.setRows(findEntry(sql, params.toArray()));
        ModelUtil.fillPageData(this, "from " + TABLE_NAME + " l inner join user u where u.userId=l.userId " + searchKeywords, data, searchParam.toArray());
        return data;
    }

    public PageData<Log> findByTypeAlias(int page, int pageSize, String typeAlias) {
        PageData<Log> data = new PageData<>();

        String sql = "select l.*,t.typeName,t.alias  as typeAlias,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId ) commentSize,u.userName from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and t.alias=? order by l.logId desc limit ?,?";
        data.setRows(find(sql, rubbish, privacy, typeAlias, ParseUtil.getFirstRecord(page, pageSize), pageSize));

        ModelUtil.fillPageData(this,
                "from " + TABLE_NAME + " l inner join user u,type t where u.userId=l.userId and t.typeId=l.typeId and rubbish=? and privacy=? and t.alias=?",
                data, new Object[]{rubbish, privacy, typeAlias});
        return data;
    }

    public Map<String, Long> getArchives() {
        List<Timestamp> lo = Db.query("select  releaseTime from " + TABLE_NAME + "  where rubbish=? and privacy=? order by releaseTime desc", rubbish, privacy);
        Map<String, Long> archives = new LinkedHashMap<>();
        for (Timestamp objects : lo) {
            if (objects != null) {
                String key = new SimpleDateFormat("yyyy_MM").format(new Date(objects.getTime()));
                if (archives.containsKey(key)) {
                    archives.put(key, archives.get(key) + 1);
                } else {
                    archives.put(key, 1L);
                }
            }
        }
        return archives;
    }

    public PageData<Log> findByTag(int page, int pageSize, String tag) {
        PageData<Log> data = new PageData<>();
        String sql = "select l.*,t.typeName,t.alias  as typeAlias,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize,u.userName from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and (l.keywords like ? or l.keywords like ? or l.keywords like ? or l.keywords= ?) order by l.logId desc limit ?,?";
        data.setRows(find(sql, rubbish, privacy, tag + ",%", "%," + tag + ",%", "%," + tag, tag, ParseUtil.getFirstRecord(page, pageSize), pageSize));
        ModelUtil.fillPageData(this,
                "from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and  (l.keywords like ? or l.keywords like ? or l.keywords like ? or l.keywords= ?)",
                data, new Object[]{rubbish, privacy, tag + ",%", "%," + tag + ",%", "%," + tag, tag});
        return data;
    }

    private List<Log> findEntry(String sql, Object[] paras) {
        return find(sql, paras);
    }

    public PageData<Log> findByDate(int page, int pageSize, String date) {
        PageData<Log> data = new PageData<>();
        String sql = "select l.*,t.typeName,t.alias as typeAlias,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId ) commentSize,u.userName from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and DATE_FORMAT(releaseTime,'%Y_%m')=? order by l.logId desc limit ?,?";
        data.setRows(find(sql, rubbish, privacy, date, ParseUtil.getFirstRecord(page, pageSize), pageSize));
        ModelUtil.fillPageData(this,
                "from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and  DATE_FORMAT(releaseTime,'%Y_%m')=?",
                data, new Object[]{rubbish, privacy, date});
        return data;
    }

    public PageData<Log> findByTitleOrPlainContentLike(int page, int pageSize, String key) {
        PageData<Log> data = new PageData<>();
        String sql = "select l.*,t.typeName,t.alias as typeAlias,(select count(commentId) from " + Comment.TABLE_NAME + " where logId=l.logId) commentSize,u.userName from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and (l.title like ? or l.plain_content like ?) order by l.logId desc limit ?,?";
        data.setRows(find(sql, rubbish, privacy, "%" + key + "%", "%" + key + "%", ParseUtil.getFirstRecord(page, pageSize), pageSize));
        ModelUtil.fillPageData(this,
                "from " + TABLE_NAME + " l inner join user u,type t where rubbish=? and privacy=? and u.userId=l.userId and t.typeId=l.typeId and (l.title like ? or l.plain_content like ?)",
                data, new Object[]{rubbish, privacy, "%" + key + "%", "%" + key + "%"});
        return data;
    }

    /**
     * @param idOrAlias
     */
    public void clickAdd(Object idOrAlias) {
        Log log = findByIdOrAlias(idOrAlias);
        if (log != null) {
            log.set("logId", log.getInt("logId")).set("click", log.getInt("click") + 1).update();
        }
    }

    public BigDecimal sumClick() {
        BigDecimal sum = findFirst("select sum(click) from " + TABLE_NAME).getBigDecimal("sum(click)");
        return sum == null ? new BigDecimal(0) : sum;
    }

    public Map<String, Object> getAttrs() {
        return super._getAttrs();
    }

    public long count() {
        return findFirst("select count(1) as count from " + TABLE_NAME + " where rubbish=? and privacy=?", false, false).getLong("count");
    }

    public long adminCount() {
        return findFirst("select count(1) as count from " + TABLE_NAME).get("count", 0);
    }
}
