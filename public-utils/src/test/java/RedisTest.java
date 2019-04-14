import com.zjy.JwtUtil.JwtUtils;
import com.zjy.redisUtil.RedisShardedPoolUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RedisTest {


    @Test
    public void save(){

        RedisShardedPoolUtil.set("blob","123");

        System.out.println(RedisShardedPoolUtil.get("blob"));

    }

    @Test
    public void jwtTest(){
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("name","blob");
        map.put("passwd","123");
        String token = JwtUtils.createJavaWebToken(map);
        System.out.println(token);
        Map<String,Object> res = JwtUtils.parserJavaWebToken(token);

    }

}
