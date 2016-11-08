package jackson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonTest {
	public static void main(String[] args) throws Exception {
		JacksonTest te = new JacksonTest();
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = new JsonFactory(mapper);
		JsonGenerator generator = factory.createGenerator(System.out);
		List<String> list = new ArrayList<>();
//		list.add("wj");
//		list.add("aj");
//		list.add(null);
		generator.writeObject(list);
		System.out.println();
		Map map = mapper.readValue("{\"ad\":\"qwe\"}",Map.class);
		List ll = mapper.readValue("[\"wj\",\"aj\",\"null\"]}",List.class);
		System.out.println(ll);
		System.out.println(map.get("ad"));
		System.out.println(map);
	}
}
