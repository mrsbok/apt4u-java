package kr.co.thefc.bbl.service;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public class DBConnService {
    protected static final String NAMESPACE = "kr.co.onedatatech.bs_hdc.main_mapper.";

    @Autowired
    private SqlSession sqlSession;

    public String selectNow(){
        return sqlSession.selectOne(NAMESPACE + "selectNow");
    }

    public String selectWithReturnString(String stat_id, HashMap map) {
        return sqlSession.selectOne(NAMESPACE + stat_id, map);
    }

    public int selectWithReturnInt(String stat_id, HashMap map) {
        return sqlSession.selectOne(NAMESPACE + stat_id, map);
    }


    public List<HashMap> select(String stat_id, HashMap map) {
        return sqlSession.selectList(NAMESPACE + stat_id, map);
    }
    public HashMap selectOne(String stat_id, HashMap map) {
        return sqlSession.selectOne(NAMESPACE + stat_id, map);
    }
    public int insertWithReturnInt(String stat_id, HashMap map) {
        int rowcnt = sqlSession.insert(NAMESPACE + stat_id, map);
        System.out.println(map.toString());
        return Integer.parseInt(map.get("insertedID").toString());
    }
    public int insertWithReturnIntList(String stat_id, HashMap map) {
        Integer rowcnt = sqlSession.insert(NAMESPACE + stat_id, map);
//        System.out.println(map.toString());
        Double idx = (Double) map.get("idx");
        Long test = Math.round(idx);
        return Integer.parseInt(test.toString());
    }
    public int insert(String stat_id, HashMap map) {
        return sqlSession.insert(NAMESPACE + stat_id, map);
    }

    public int insertList(String stat_id, List list) {
        return sqlSession.insert(NAMESPACE + stat_id, list);
    }

    public HashMap selectIdx(String stat_id, Integer idx) {
        return sqlSession.selectOne(NAMESPACE + stat_id, idx);
    }
    public List<HashMap> selectIdxList(String stat_id, Integer idx) {
        return sqlSession.selectList(NAMESPACE + stat_id, idx);
    }
    public int update(String stat_id, HashMap map) {
        return sqlSession.update (NAMESPACE + stat_id, map);
    }

    public int delete(String stat_id, HashMap map) {
        return sqlSession.delete(NAMESPACE + stat_id, map);
    }


}
