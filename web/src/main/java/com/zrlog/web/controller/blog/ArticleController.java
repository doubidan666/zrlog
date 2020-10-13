package com.zrlog.web.controller.blog;

import com.zrlog.business.cache.CacheService;
import com.zrlog.business.rest.request.CreateCommentRequest;
import com.zrlog.business.rest.response.CreateCommentResponse;
import com.zrlog.business.service.ArticleService;
import com.zrlog.business.service.CommentService;
import com.zrlog.business.util.PagerUtil;
import com.zrlog.common.Constants;
import com.zrlog.common.rest.request.PageRequest;
import com.zrlog.common.vo.AdminTokenVO;
import com.zrlog.data.dto.PageData;
import com.zrlog.model.Comment;
import com.zrlog.model.Log;
import com.zrlog.model.Type;
import com.zrlog.util.I18nUtil;
import com.zrlog.util.ParseUtil;
import com.zrlog.util.ZrLogUtil;
import com.zrlog.web.controller.BaseController;
import com.zrlog.web.handler.BlogArticleHandler;
import com.zrlog.web.token.AdminTokenService;
import com.zrlog.web.util.WebTools;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.math.BigDecimal;

public class ArticleController extends BaseController {

    private final ArticleService articleService = new ArticleService();

    private final CommentService commentService = new CommentService();

    private final AdminTokenService adminTokenService = new AdminTokenService();

    /**
     * add page info for template more easy
     *
     * @param currentUri
     * @param data
     * @param pageRequest
     */
    private void setPageInfo(String currentUri, PageData<Log> data, PageRequest pageRequest) {
        setAttr("yurl", currentUri);
        setAttr("data", data);
        long totalPage = BigDecimal.valueOf(Math.ceil(data.getTotalElements() * 1.0 / pageRequest.getSize())).longValue();
        if (totalPage > 1) {
            setAttr("pager", PagerUtil.generatorPager(currentUri, pageRequest.getPage(), totalPage));
        }
    }

    public String index() {
        if (getPara(0) != null) {
            if ("all".equals(getPara(0))) {
                return all();
            } else if (getPara(0) != null) {
                return detail();
            } else {
                return all();
            }
        } else {
            return all();
        }
    }

    public String search() {
        String key;
        PageData<Log> data;
        if (getParaToInt(1) == null) {
            if (isNotNullOrNotEmptyStr(getPara("key"))) {
                if ("GET".equals(getRequest().getMethod())) {
                    key = convertRequestParam(getPara("key"));
                } else {
                    key = getPara("key");
                }
                data = articleService.searchArticle(1, getDefaultRows(), key);
            } else {
                return all();
            }

        } else {
            key = convertRequestParam(getPara(0));
            data = articleService.searchArticle(getParaToInt(1), getDefaultRows(), key);
        }
        // 记录回话的Key
        setAttr("key", WebTools.htmlEncode(key));

        setAttr("tipsType", I18nUtil.getStringFromRes("search"));
        setAttr("tipsName", WebTools.htmlEncode(key));

        setPageInfo(Constants.getArticleUri() + "search/" + key + "-", data, new PageRequest(getParaToInt(1, 1), getDefaultRows()));
        return "page";
    }

    public String record() {
        setAttr("tipsType", I18nUtil.getStringFromRes("archive"));
        setAttr("tipsName", getPara(0));

        setPageInfo(Constants.getArticleUri() + "record/" + getPara(0) + "-", new Log().findByDate(getParaToInt(1, 1), getDefaultRows(), getPara(0)), new PageRequest(getParaToInt(1, 1), getDefaultRows()));
        return "page";
    }

    public void addComment() {
        CreateCommentResponse response = saveComment();
        String ext = "";
        if (Constants.isStaticHtmlStatus()) {
            ext = ".html";
            new CacheService().refreshInitDataCache(BlogArticleHandler.CACHE_HTML_PATH, this, true);
        }
        redirect("/" + Constants.getArticleUri() + response.getAlias() + ext);
    }

    CreateCommentResponse saveComment() {
        CreateCommentRequest createCommentRequest = ZrLogUtil.convertRequestParam(getRequest().getParameterMap(), CreateCommentRequest.class);
        createCommentRequest.setIp(WebTools.getRealIp(getRequest()));
        createCommentRequest.setUserAgent(Jsoup.clean(getHeader("User-Agent"), Whitelist.basic()));
        return commentService.save(createCommentRequest);
    }

    public String detail() {
        return detail(convertRequestParam(getPara()));
    }

    protected String detail(Object id) {
        AdminTokenVO adminTokenVO = adminTokenService.getAdminTokenVO(getRequest());
        Log log;
        if (adminTokenVO == null) {
            log = new Log().findByIdOrAlias(id);
        } else {
            log = new Log().adminFindByIdOrAlias(id);
        }
        if (log == null) {
            return "404";
        }
        Integer logId = log.get("logId");
        log.put("lastLog", new Log().findLastLog(logId, I18nUtil.getStringFromRes("noLastLog")));
        log.put("nextLog", new Log().findNextLog(logId, I18nUtil.getStringFromRes("noNextLog")));
        log.put("comments", new Comment().findAllByLogId(logId));
        setAttr("log", log);
        return "detail";

    }

    public String sort() {
        String typeStr = convertRequestParam(getPara(0));
        setPageInfo(Constants.getArticleUri() + "sort/" + typeStr + "-", new Log().findByTypeAlias(getParaToInt(1, 1), getDefaultRows(), typeStr), new PageRequest(getParaToInt(1, 1), getDefaultRows()));

        Type type = new Type().findByAlias(typeStr);
        setAttr("type", type);
        setAttr("tipsType", I18nUtil.getStringFromRes("category"));
        if (type != null) {
            setAttr("tipsName", type.getStr("typeName"));
        }
        return "page";
    }

    public String tag() {
        if (getPara(0) != null) {
            String tag = convertRequestParam(getPara(0));
            setPageInfo(Constants.getArticleUri() + "tag/" + getPara(0) + "-", new Log().findByTag(getParaToInt(1, 1), getDefaultRows(), tag), new PageRequest(getParaToInt(1, 1), getDefaultRows()));

            setAttr("tipsType", I18nUtil.getStringFromRes("tag"));
            setAttr("tipsName", tag);
        }
        return "page";
    }

    public String tags() {
        return "tags";
    }

    public String link() {
        return "link";
    }

    public String all() {
        PageRequest pageRequest = new PageRequest(ParseUtil.strToInt(getPara(1), 1), getDefaultRows());
        PageData<Log> data = new Log().find(pageRequest);
        setPageInfo(Constants.getArticleUri() + "all-", data, pageRequest);
        return "index";
    }
}
