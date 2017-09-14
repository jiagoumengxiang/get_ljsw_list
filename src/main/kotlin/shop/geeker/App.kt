package shop.geeker

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File

class Article{
    lateinit var title:String
    lateinit var content:MutableList<String>
}
fun get_article_content(url:String):Article{
    var doc : Document? = Jsoup.connect(url).get()
    var article = Article()
    if(doc!=null){
        article.title = doc.select(".desc").text()
        var page_node:Elements? = doc.select(".rich_media_content p")
        if (page_node != null) {
            article.content = ArrayList<String>()
            for(line in page_node){
                if(line!=null){
                    article.content.add(line.text())
                }
            }
        }
    }
    return article
}

class Page(num : String,url:String){
    var num = num
    var url = url
}
fun get_current_pages(doc:Document):List<Page>{
    var pages = doc.select(".pages a")
    var pageList = ArrayList<Page>()
    if(pages!=null){
        for(pageData in pages){
            pageList.add(Page(pageData.text(),pageData.attr("href")))
        }
    }
    return pageList
}

class ArticleLink(title:String,url:String){
    var title:String = title
    var url:String = url
}
fun get_current_articles(doc:Document):List<ArticleLink>{
    val items = doc.select(".news_desc h3 a")
    var articleLinks = ArrayList<ArticleLink>()
    if(items!=null){
        for(articleLink in items) {
            if (articleLink.text().indexOf("清单") >= 0) {
                articleLinks.add(ArticleLink(articleLink.text(), articleLink.attr("href")))
            }
        }
    }
    return articleLinks
}

tailrec fun App(page:Page,articleLinks:List<ArticleLink>): List<Article>? {

    var doc = Jsoup.connect(page.url).get()
    if(doc!=null){

        //获取当前页文章列表
        val currentArticleLinks = get_current_articles(doc)

        //获取下一页地址
        val currentPages = get_current_pages(doc)
        val nextPage = currentPages.filter { it.num == (page.num.toInt()+1).toString() }

        if (nextPage.isEmpty()){
            //获取文章
            val real_articleLinks = articleLinks.plus(currentArticleLinks)
            val articleList = real_articleLinks.map { get_article_content(it.url) }
            return articleList
        }else{
            return App(nextPage.get(0),currentArticleLinks.plus(articleLinks))
        }
    }else{
        return null
    }
}

fun main(args:Array<String>){

    val page_30 = Page("1","http://www.anyv.net/index.php/account-10184-page-1")

    val articles = App(page_30, emptyList())
    val file = File("清单.org")
    if (articles != null) {
        for(article in articles){
                println(article.title)
                file.appendText("* "+article.title)
                file.appendText("\n")
                for (line in article.content) {
                    if(line.length>0 && "123456789".indexOf(line.get(0))>-1) {
                        println(line)
                        file.appendText(line)
                        file.appendText("\n")
                    }
                }
        }
    }


    //var article = get_article_content("http://www.anyv.net/index.php/article-1565836")
    //println(article.content.size)

//    val db = DBMaker.fileDB("file.db").make()
//    val map :HTreeMap<String,String> = db.hashMap("map").createOrOpen() as HTreeMap<String, String>
//    map.put("abc","bcd")
//    println(map.get("abc"))
//    db.close()
}
