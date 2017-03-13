package cn.itcast.lucene;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class QueryTest {
	
	//查询分析器搜索
	@Test
	public void testQueryParser() throws Exception {
		//创建查询分析器，指定查询域和分词器
		QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());
		Query query = queryParser.parse("Apple iPhone 7 Plus (A1661) 128G 黑色 移动联通电信4G手机");
		System.out.println(query);
		doSearch(query);
	}
	
	
	//组合搜索
	@Test
	public void testBooleanQuery() throws Exception{
		Query query1 = new TermQuery(new Term("title", "apple"));
		Query query2 = NumericRangeQuery.newLongRange("id", 10L, 50L, false, true);
		BooleanQuery query = new BooleanQuery();
		query.add(query1, Occur.SHOULD);
		query.add(query2, Occur.MUST);
		System.out.println(query);
		doSearch(query);
	}
	
	//相似度搜索
	@Test
	public void testFuzzyQuery() throws Exception {
		Query query = new FuzzyQuery(new Term("title", "appld"));
		doSearch(query);
	}
	
	//数值范围
	@Test
	public void testNumericRangeQuery() throws Exception{
		//字段，最小值，最大值，包含不包含最小值，包含不包含最大值
		Query query = NumericRangeQuery.newLongRange("id", 10L, 50L, false, true);
		doSearch(query);
	}
	
	//词条
	@Test
	public void testTermQuery() throws Exception {
		Query query = new TermQuery(new Term("title", "apple"));
		doSearch(query);
	}
	
	//搜索索引
	private void doSearch(Query query) throws Exception{
		//创建索引目录
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		//创建索引读入器
		IndexReader indexReader = DirectoryReader.open(directory);
		//创建索引搜索器
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		//第一参数：查询对象，第二个参数：符合本次查询的前几个文档
		TopDocs topDocs = indexSearcher.search(query, 10);
		//本次符合查询条件的总记录数
		System.out.println("本次符合查询条件的总记录数为：" + topDocs.totalHits);
		//得分文档数组，可以获取到符合查询的文档id
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document document = indexSearcher.doc(scoreDoc.doc);
			System.out.println("----------------------------------------");
			System.out.println("文档在lucene中对应的id为：" + scoreDoc.doc);
			System.out.println("id为：" + document.get("id"));
			System.out.println("标题为：" + document.get("title"));
			System.out.println("价格为：" + document.get("price"));
			System.out.println("卖点为：" + document.get("sellPoint"));
			System.out.println("图片为：" + document.get("image"));
			System.out.println("状态为：" + document.get("status"));
		}
	}
	
	//写索引
	@Test
	public void testWriteIndex() throws Exception{
		//创建索引目录
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		//创建索引分词器
		//StandardAnalyzer analyzer = new StandardAnalyzer();
		//IK分词器
		IKAnalyzer analyzer = new IKAnalyzer();
		//创建写索引配置对象
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		//创建写索引对象
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		
		//创建文档
		for (int i = 0; i < 100; i++) {
			Document document = new Document();
			document.add(new LongField("id", i, Store.YES));
			TextField textField = new TextField("title", "Apple iPhone 7 Plus (A1661) 128G 黑色 移动联通电信4G手机--- " +i, Store.YES);
			if(i%7==0){
				//设置相关度
				textField.setBoost(2.0F);
			}
			document.add(textField);
			document.add(new LongField("price", 1000+i, Store.YES));
		    document.add(new StringField("image", "https://item.jd.com/3133857.html", Store.YES));
		    document.add(new StringField("sellPoint", "iPhone6 32G 金 新品上市！“金”典呈现！新品下单享6期白条免息！", Store.YES));
		    document.add(new IntField("status", 1, Store.NO));
		    
		    //添加文档（相当于数据库中的一条记录（好些字段））
		    indexWriter.addDocument(document);
		}
		//提交并关闭
		indexWriter.commit();
		indexWriter.close();
	}
	
}