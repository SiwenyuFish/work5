package siwenyu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface GroupMapper {


    @Select("select `group`.members from `group`where binary name=#{groupname}")
    String getMembersByGroupName(String groupname);

    @Insert("insert into `group` (number, name, members) VALUES (#{snowFlakeId},#{groupname},#{members})")
    void saveGroup(Long snowFlakeId, String groupname, String members);
}
