package com.roy.github.learn.thoughtworks;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.List;

/**
 *        <dependency>
 *             <groupId>com.thoughtworks.xstream</groupId>
 *             <artifactId>xstream</artifactId>
 *             <version>1.4.10</version>
 *         </dependency>
 */
public class XstreamTest {

    public static void main(String[] args) {
        XStream xStream = new XStream(new DomDriver());
        Profile profileBean = new Profile("job1","remark");
        System.out.println(xStream.toXML(profileBean));
        String xml = "<com.roy.github.learn.thoughtworks.XstreamTest_-Profile>\n" +
                "  <job>job1</job>\n" +
                "  <remark>remark</remark>\n" +
                "</com.roy.github.learn.thoughtworks.XstreamTest_-Profile>";
        profileBean = (Profile) xStream.fromXML(xml);
        System.out.println(profileBean.job+" "+ profileBean.getRemark());

        Address address1 = new Address("郑州市经三路", "450001");
        Address address2 = new Address("北京市海淀区", "100000");
        List<Address> addList = new ArrayList<Address>();
        addList.add(address1);
        addList.add(address2);
        Profile profile = new Profile("软件工程师", "备注说明");
        Person person = new Person("X-rapido", "22", profile, addList);

        xml = xStream.toXML(person);
        System.out.println("----------------第1次输出, 不设置类别名---------------- \n"+ xml + "\n");

        xStream.alias("PERSON", Person.class);
        xStream.alias("PROFILE", Profile.class);
        xStream.alias("ADDRESS", Address.class);
        xml = xStream.toXML(person);
        System.out.println("----------------第2次输出, 设置类别名---------------- \n"+ xml + "\n");

        xStream.aliasField("Name", Person.class, "name");
        /*
         * [注意] 设置Person类的profile成员别名PROFILE,这个别名和Profile类的别名一致,
         * 这样可以保持XStream对象可以从profile成员生成的xml片段直接转换为Profile成员,
         * 如果成员profile的别名和Profile的别名不一致,则profile成员生成的xml片段不可
         * 直接转换为Profile对象,需要重新创建XStream对象,这岂不给自己找麻烦?
         */
        xStream.aliasField("PROFILE", Person.class, "profile");
        xStream.aliasField("ADDLIST", Person.class, "addlist");
        xStream.aliasField("Job", Profile.class, "job");
        xml = xStream.toXML(person);
        System.out.println("----------------第3次输出, 设置类、字段别名---------------- \n"+ xml + "\n");




    }


    public static class Person{
        /**
         * name :
         * age :
         * profile : {"job":"","remark":""}
         * addList : [{"zipCode":"","address":""}]
         */

        private String name;
        private String age;
        private Profile profile;
        private List<Address> addList;

        public Person(String name,String age,Profile profile,List<Address> addList){
            this.name=name;
            this.age=age;
            this.profile=profile;
            this.addList=addList;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public List<Address> getAddList() {
            return addList;
        }

        public void setAddList(List<Address> addList) {
            this.addList = addList;
        }
    }

    public static class Profile {
        /**
         * job :
         * remark :
         */

        private String job;
        private String remark;

        public Profile(String job,String remark) {
            this.job = job;
            this.remark = remark;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class Address {
        /**
         * zipCode :
         * address :
         */

        private String zipCode;
        private String address;
        public Address(String zipCode, String address) {
            this.zipCode = zipCode;
            this.address = address;
        }
        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }
}
