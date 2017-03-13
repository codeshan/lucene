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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;


public class LuceneTest  {
	
	
	//搜索索引
	@Test
	public void testDoSearch() throws Exception{
		//指定索引目录FSDirectory；调用open
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		
		//创建索引读对象InderReader；指定索引目录
		IndexReader indexReader = DirectoryReader.open(directory);
		
		//创建索引搜索器
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		
		//查询对象；使用最小查询单位
		Query query = new TermQuery(new Term("title", "apple"));
		
		//第一参数：查询对象，第二个参数：符合本次查询的前几个文档
		TopDocs topDocs = indexSearcher.search(query, 10);
		
		//本次符合查询条件的总记录数
		System.out.println("本次符合查询条件的总记录数为：" + topDocs.totalHits);
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println("----------------------------------------");
			System.out.println("文档在lucene中对应的id为：" + scoreDoc.doc);
			System.out.println("id为：" + doc.get("id"));
			System.out.println("标题为：" + doc.get("title"));
			System.out.println("价格为：" + doc.get("price"));
			System.out.println("卖点为：" + doc.get("sellPoint"));
			System.out.println("图片为：" + doc.get("image"));
			System.out.println("状态为：" + doc.get("status"));
		}
		
	}
	
	//创建索引
	@Test
	public void testIndexWriter() throws Exception{
		
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		// 创建标准分词器
		//StandardAnalyzer analyzer = new StandardAnalyzer();
		//IK分词器--对中文支持较好
		IKAnalyzer analyzer = new IKAnalyzer();
		// 创建索引写配置对象
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		// 对索引目录内容全新创建
		//indexWriterConfig.setOpenMode(OpenMode.CREATE);
		indexWriterConfig.setOpenMode(OpenMode.APPEND);
		// 创建索引写对象
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		// 创建文档对象
		Document document = new Document();
		document.add(new IntField("id", 12, Store.YES));
		document.add(new TextField("title", "111---Apple iPhone 6s Plus (A1699) 64G 金色 移动联通电信4G手机", Store.YES));
		document.add(new LongField("price", 6488L, Store.YES));
		document.add(new StringField("pic", "http://item.jd.com/bigimage.aspx?id=1861095", Store.YES));
		// 添加要写成索引的文档
		indexWriter.addDocument(document);
		
		//提交并关闭
		indexWriter.commit();
		indexWriter.close();
		
	}
	
	@Test
	public void testUpdateIndex() throws Exception{
		//创建索引目录
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		//创建标准分词器
		StandardAnalyzer analyzer = new StandardAnalyzer();
		//创建索引配置对象
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		//indexWriterConfig.setOpenMode(OpenMode.CREATE);	//对索引目录全新创建
		//indexWriterConfig.setOpenMode(OpenMode.APPEND);	//对索引目录追加文档
		//创建索引写对象
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		//创建文档
		Document document = new Document();
		document.add(new LongField("id", 3133857L, Store.YES));
		document.add(new TextField("title", "11111 Apple iPhone 7 Plus (A1661) 128G 黑色 移动联通电信4G手机", Store.YES));
		document.add(new LongField("price", 6499L, Store.YES));
		document.add(new StringField("image", "https://item.jd.com/3133857.html", Store.YES));
		document.add(new StringField("sellPoint", "iPhone6 32G 金 新品上市！“金”典呈现！新品下单享6期白条免息！", Store.YES));
		document.add(new IntField("status", 1, Store.NO));
		//更新索引库中的索引
		//第一个参数是更新的条件；第二个参数更新的文档
		//不管有多少条数据是符合更新条件的，更新之后只会存在一条文档（就是新的这个文档）
		indexWriter.updateDocument(new Term("title", "apple"), document);
		
		//提交并关闭
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void testDeleteIndex() throws Exception{
		//创建索引目录
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		//创建标准的分词器
		StandardAnalyzer analyzer = new StandardAnalyzer();
		//创建写索引的配置对象
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		//indexWriterConfig.setOpenMode(OpenMode.CREATE);//对索引目录全新创建
		//indexWriterConfig.setOpenMode(OpenMode.APPEND);//对索引目录追加文档
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		//条件删除索引库中的索引
		indexWriter.deleteDocuments(new Term("title","apple"));
		
		//提交和关闭
		indexWriter.commit();
		indexWriter.close();
	}
	
	@Test
	public void testDeleteAllIndex() throws Exception{
		Directory directory = FSDirectory.open(new File("D:/study/index"));
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		indexWriter.deleteAll();
		indexWriter.commit();
		indexWriter.close();
	}
	
}
