package mongo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.Binary;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.operation.GroupOperation;

public class MGDatabase {

	public static void main(String[] args) throws Exception {
		MongoClient mongo = new MongoClient("10.135.112.60", 27017);
		MongoClientOptions options = mongo.getMongoClientOptions();
		System.out.println(options);
		MongoDatabase db = mongo.getDatabase("wj");
		MongoCollection<Document> collection = db.getCollection("col");
		// Document doc = new Document();
		// doc.put("age", 11);
		Document doc = new Document();
		doc.put("age", new Document("$gt", 11));
		// 删除
		// collection.insertOne(doc);
		// collection.deleteMany(doc);
		Document inc = new Document();
		inc.put("age", new Document("age", new Document("$inc", 2)));// 更新失败
		// collection.insertOne(inc);
		collection
				.updateMany(doc, new Document("$inc", new Document("age", 2)));
		FileInputStream fin = new FileInputStream("D:\\资料\\tcp.JPG");
		byte[] b = new byte[fin.available()];
		fin.read(b);
		fin.close();
		// -----存储二进制
		// collection.insertOne(new Document("file", new BsonBinary(b)));
		// 上面那种查询不方便,使用下面这种,查询方便
		// Document file = new Document("file", "fileName");
		// file.put("bin",new BsonBinary(b));
		// collection.insertOne(file);

		// ------读取
		// Document pic = collection.find(new Document("file",
		// "fileName")).first();
		// Binary binary = (Binary) pic.get("bin");
		// FileOutputStream fou = new FileOutputStream("D:\\tcp.jpg");
		// fou.write(binary.getData());
		// fou.close();
		// -----符合条件查询,跟控制台的Bson思想一样
		BsonArray bsonArray = new BsonArray();
		bsonArray.add(new BsonString("wj_11"));
		bsonArray.add(new BsonString("wj_12"));
		print(collection.find().filter(
				new Document("name", new Document("$in", bsonArray))));
		// collection.distinct("", MGDatabase.class);
		// GroupOperation<Document> group = new GroupOperation<>(namespace,
		// reduceFunction, initial, decoder);
		MapReduceIterable<Document> mapReduce = collection.mapReduce(
				"function(){" + "  for(var key in this){"
						+ "    emit(key,{ad:key,count:1});" // 一个key维护多个文档,当key相同的时候
						+ "  }" + "};", "function(key,emits){"
						+ " var total = 0;" + " emits.forEach(function(val){"
						+ "     total+=val.count;" + " });"
						+ " return {ad:key,\"count\":total};" // 返回为文档形式,这才是最后reduce的结果集,以上的在数量为1的时候才合进去
						+ "};");
		// -----
		MongoCursor<Document> it = mapReduce.iterator();
		while (it.hasNext()) {
			Document next = it.next();
			System.out.println(next);
			System.out.println(next.get("_id"));
		}
		mongo.close();
	}

	public static void print(FindIterable<Document> ss) {
		MongoCursor<Document> it = ss.iterator();
		while (it.hasNext()) {
			Document next = it.next();
			System.out.println(next);
			System.out.println(next.getObjectId("_id"));// ObjectID对象,可以获取到时间之类的很多属性
		}
	}
}