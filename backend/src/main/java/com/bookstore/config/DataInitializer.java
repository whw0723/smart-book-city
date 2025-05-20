package com.bookstore.config;

import com.bookstore.entity.Admin;
import com.bookstore.entity.Book;
import com.bookstore.entity.User;
import com.bookstore.mapper.AdminMapper;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 初始化管理员数据
        if (adminMapper.findAll().isEmpty()) {
            // 创建管理员用户
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("123456");
            admin.setEmail("admin@example.com");
            admin.setPhone("13800138000");
            admin.setCreateTime(new Date());
            admin.setUpdateTime(new Date());
            adminMapper.save(admin);
        }

        // 初始化普通用户数据
        if (userMapper.findAll().isEmpty()) {
            // 创建普通用户
            User user = new User();
            user.setUsername("user");
            user.setPassword("123456");
            user.setEmail("user@example.com");
            user.setRole(0); // 普通用户角色
            userMapper.save(user);
        }

        // 初始化图书数据
        if (bookMapper.findAll().isEmpty()) {
            // 文学小说类 - novel
            createBook("三体", "刘慈欣", new BigDecimal("23.00"), 100, "novel", "刘慈欣代表作，亚洲首部获得雨果奖的科幻巨著");
            createBook("活着", "余华", new BigDecimal("20.00"), 200, "novel", "余华代表作，讲述了人如何活着的故事");
            createBook("百年孤独", "加西亚·马尔克斯", new BigDecimal("35.50"), 120, "novel", "魔幻现实主义文学的代表作");
            createBook("围城", "钱钟书", new BigDecimal("28.00"), 150, "novel", "中国现代文学经典，描写知识分子的生活");
            createBook("平凡的世界", "路遥", new BigDecimal("68.00"), 180, "novel", "中国当代文学经典，反映改革开放初期农村生活");
            createBook("红楼梦", "曹雪芹", new BigDecimal("42.00"), 130, "novel", "中国古典四大名著之一");
            createBook("西游记", "吴承恩", new BigDecimal("30.00"), 140, "novel", "中国古典四大名著之一");
            createBook("三国演义", "罗贯中", new BigDecimal("35.00"), 120, "novel", "中国古典四大名著之一");
            createBook("水浒传", "施耐庵", new BigDecimal("36.00"), 110, "novel", "中国古典四大名著之一");
            createBook("悲惨世界", "维克多·雨果", new BigDecimal("45.00"), 90, "novel", "法国文学巨著，讲述善与恶的故事");
            createBook("追风筝的人", "卡勒德·胡赛尼", new BigDecimal("29.80"), 160, "novel", "阿富汗背景下的救赎故事");
            createBook("挪威的森林", "村上春树", new BigDecimal("27.80"), 170, "novel", "村上春树代表作，青春与爱情的故事");
            createBook("时间简史", "斯蒂芬·霍金", new BigDecimal("38.00"), 80, "novel", "探索宇宙奥秘的科普名著");
            createBook("月亮与六便士", "毛姆", new BigDecimal("25.00"), 130, "novel", "关于理想与现实的经典小说");
            
            // 科技计算机类 - technology
            createBook("JavaScript高级程序设计", "Nicholas C. Zakas", new BigDecimal("99.00"), 50, "technology", "JavaScript领域经典图书，入门与提高必读");
            createBook("深入理解计算机系统", "Randal E. Bryant", new BigDecimal("115.00"), 40, "technology", "计算机系统底层原理解析，程序员必读书籍");
            createBook("算法导论", "Thomas H. Cormen", new BigDecimal("128.00"), 30, "technology", "算法领域的圣经级教材");
            createBook("代码大全", "Steve McConnell", new BigDecimal("108.00"), 60, "technology", "软件构建实践指南，编程技巧全书");
            createBook("人工智能：一种现代方法", "Stuart Russell", new BigDecimal("145.00"), 35, "technology", "AI领域经典教材，全面介绍人工智能技术");
            createBook("Spring实战", "Craig Walls", new BigDecimal("89.00"), 70, "technology", "Spring框架实战指南");
            createBook("Python编程：从入门到实践", "Eric Matthes", new BigDecimal("79.00"), 90, "technology", "Python学习入门经典教程");
            createBook("图解HTTP", "上野宣", new BigDecimal("49.00"), 80, "technology", "通俗易懂的HTTP协议入门书");
            createBook("Java核心技术", "Cay S. Horstmann", new BigDecimal("111.00"), 60, "technology", "Java技术的权威指南");
            createBook("设计模式", "Erich Gamma", new BigDecimal("78.00"), 65, "technology", "软件设计模式的开山之作");
            createBook("编程珠玑", "Jon Bentley", new BigDecimal("59.00"), 75, "technology", "编程技巧与思想的经典");
            createBook("数据结构与算法分析", "Mark Allen Weiss", new BigDecimal("85.00"), 50, "technology", "数据结构与算法学习指南");
            createBook("机器学习", "周志华", new BigDecimal("88.00"), 40, "technology", "机器学习入门经典教材");
            createBook("Clean Code", "Robert C. Martin", new BigDecimal("69.00"), 55, "technology", "编写整洁代码的艺术");
            
            // 社会科学类 - education
            createBook("教育心理学", "安妮·伍尔福克", new BigDecimal("68.00"), 60, "education", "教育心理学经典教材");
            createBook("乌合之众", "古斯塔夫·勒庞", new BigDecimal("32.00"), 100, "education", "群体心理研究的经典之作");
            createBook("社会学概论", "安东尼·吉登斯", new BigDecimal("78.00"), 75, "education", "社会学入门教材");
            createBook("人类简史", "尤瓦尔·赫拉利", new BigDecimal("68.00"), 120, "education", "从人类进化角度看世界历史");
            createBook("思考，快与慢", "丹尼尔·卡尼曼", new BigDecimal("59.50"), 90, "education", "行为经济学经典著作");
            createBook("沟通的艺术", "罗纳德·阿德勒", new BigDecimal("45.00"), 80, "education", "人际沟通技巧指南");
            createBook("影响力", "罗伯特·西奥迪尼", new BigDecimal("42.00"), 85, "education", "心理学经典著作，关于说服与影响力");
            createBook("社会契约论", "卢梭", new BigDecimal("28.00"), 70, "education", "政治哲学经典著作");
            createBook("教育学原理", "王道俊", new BigDecimal("45.00"), 60, "education", "教育学基础理论教材");
            createBook("批判性思维", "理查德·保罗", new BigDecimal("39.00"), 65, "education", "培养批判性思维能力的指南");
            createBook("心理学与生活", "理查德·格里格", new BigDecimal("88.00"), 50, "education", "心理学入门经典教材");
            createBook("哲学导论", "尼古拉斯·沃尔特斯托夫", new BigDecimal("52.00"), 55, "education", "西方哲学入门读物");
            createBook("逻辑学导论", "欧文·科皮", new BigDecimal("48.00"), 45, "education", "逻辑学基础教材");
            createBook("公正：该如何做是好？", "迈克尔·桑德尔", new BigDecimal("38.00"), 95, "education", "哈佛大学公开课基础读物");
            
            // 经济管理类 - economics
            createBook("国富论", "亚当·斯密", new BigDecimal("65.00"), 50, "economics", "经济学奠基之作");
            createBook("经济学原理", "曼昆", new BigDecimal("88.00"), 70, "economics", "经济学入门经典教材");
            createBook("怪诞行为学", "丹·艾瑞里", new BigDecimal("42.00"), 80, "economics", "行为经济学经典著作");
            createBook("金融学", "兹维·博迪", new BigDecimal("95.00"), 40, "economics", "金融学基础教材");
            createBook("管理学", "斯蒂芬·罗宾斯", new BigDecimal("78.00"), 60, "economics", "管理学经典教材");
            createBook("从优秀到卓越", "吉姆·柯林斯", new BigDecimal("45.00"), 85, "economics", "管理实践经典著作");
            createBook("薪酬管理", "乔治·米尔科维奇", new BigDecimal("68.00"), 35, "economics", "人力资源管理专业教材");
            createBook("市场营销学", "菲利普·科特勒", new BigDecimal("75.00"), 50, "economics", "市场营销学经典教材");
            createBook("投资学", "威廉·夏普", new BigDecimal("86.00"), 45, "economics", "投资理论与实践教材");
            createBook("穷查理宝典", "查理·芒格", new BigDecimal("58.00"), 75, "economics", "投资智慧集锦");
            createBook("公司财务原理", "理查德·布雷利", new BigDecimal("92.00"), 30, "economics", "公司财务管理教材");
            createBook("战略管理", "迈克尔·波特", new BigDecimal("78.00"), 40, "economics", "企业战略管理经典");
            createBook("商业模式新生代", "亚历山大·奥斯特瓦德", new BigDecimal("49.00"), 65, "economics", "商业模式设计与创新指南");
            createBook("组织行为学", "斯蒂芬·罗宾斯", new BigDecimal("72.00"), 50, "economics", "组织行为学经典教材");
            
            // 历史传记类 - history
            createBook("万历十五年", "黄仁宇", new BigDecimal("39.50"), 70, "history", "从细节透视明朝历史");
            createBook("明朝那些事儿", "当年明月", new BigDecimal("328.00"), 80, "history", "通俗明朝历史读物");
            createBook("人类群星闪耀时", "茨威格", new BigDecimal("36.00"), 65, "history", "历史转折点的人物群像");
            createBook("第二次世界大战回忆录", "丘吉尔", new BigDecimal("198.00"), 40, "history", "二战亲历者的历史记录");
            createBook("拿破仑传", "埃米尔·路德维希", new BigDecimal("58.00"), 55, "history", "拿破仑一世的传记作品");
            createBook("史记", "司马迁", new BigDecimal("125.00"), 60, "history", "中国第一部纪传体通史");
            createBook("资治通鉴", "司马光", new BigDecimal("158.00"), 45, "history", "中国古代编年体通史");
            createBook("光荣与梦想", "威廉·曼彻斯特", new BigDecimal("168.00"), 35, "history", "美国现代史1932-1972年");
            createBook("枪炮、病菌与钢铁", "贾雷德·戴蒙德", new BigDecimal("59.00"), 75, "history", "人类社会发展史");
            createBook("希特勒传", "约阿希姆·费斯特", new BigDecimal("88.00"), 30, "history", "客观记录纳粹德国领导人生平");
            createBook("汉密尔顿传", "罗恩·彻诺", new BigDecimal("68.00"), 50, "history", "美国开国元勋的传记");
            createBook("中国通史", "吕思勉", new BigDecimal("78.00"), 60, "history", "中国历史通俗读本");
            createBook("世界简史", "威尔斯", new BigDecimal("55.00"), 70, "history", "世界历史概览");
            createBook("中国历代政治得失", "钱穆", new BigDecimal("38.00"), 80, "history", "中国政治制度史研究");
            
            // 艺术设计类 - art
            createBook("写给大家看的设计书", "Robin Williams", new BigDecimal("59.00"), 70, "art", "平面设计入门经典");
            createBook("艺术的故事", "贡布里希", new BigDecimal("128.00"), 50, "art", "西方艺术史经典著作");
            createBook("设计心理学", "唐纳德·诺曼", new BigDecimal("65.00"), 60, "art", "设计与人类心理的关系");
            createBook("配色设计原理", "佩妮·伯克", new BigDecimal("78.00"), 55, "art", "色彩搭配与应用指南");
            createBook("摄影的艺术", "布鲁斯·巴纳姆", new BigDecimal("85.00"), 45, "art", "摄影技巧与艺术表现");
            createBook("素描的诀窍", "伯特·多德森", new BigDecimal("48.00"), 65, "art", "素描入门基础教程");
            createBook("版式设计原理", "蒂莫西·萨马拉", new BigDecimal("75.00"), 40, "art", "版面设计与排版指南");
            createBook("中国美术史", "中央美术学院", new BigDecimal("98.00"), 30, "art", "中国美术发展历程");
            createBook("色彩心理学", "伊顿", new BigDecimal("56.00"), 50, "art", "色彩对人类心理的影响研究");
            createBook("建筑的意象", "凯文·林奇", new BigDecimal("45.00"), 60, "art", "城市设计与建筑规划经典");
            createBook("西方音乐简史", "保罗·亨利·朗", new BigDecimal("68.00"), 35, "art", "西方音乐发展历程");
            createBook("服装设计入门", "苏珊娜·亚西尔", new BigDecimal("72.00"), 45, "art", "服装设计基础知识");
            createBook("电影艺术", "大卫·波德维尔", new BigDecimal("82.00"), 30, "art", "电影制作与鉴赏");
            createBook("字体设计手册", "简·蒂格", new BigDecimal("89.00"), 40, "art", "字体设计与应用指南");
            
            // 儿童读物类 - children
            createBook("小王子", "安东尼·德·圣-埃克苏佩里", new BigDecimal("22.00"), 150, "children", "经典儿童文学作品");
            createBook("夏洛的网", "E.B.怀特", new BigDecimal("25.00"), 120, "children", "关于友谊的儿童文学名著");
            createBook("窗边的小豆豆", "黑柳彻子", new BigDecimal("26.50"), 140, "children", "日本儿童教育小说");
            createBook("绿山墙的安妮", "露西·莫德·蒙格马利", new BigDecimal("28.00"), 110, "children", "经典儿童成长小说");
            createBook("哈利·波特与魔法石", "J.K.罗琳", new BigDecimal("32.50"), 200, "children", "魔法冒险系列小说");
            createBook("爱的教育", "亚米契斯", new BigDecimal("18.00"), 130, "children", "意大利儿童文学经典");
            createBook("安徒生童话", "安徒生", new BigDecimal("35.00"), 160, "children", "世界著名童话故事集");
            createBook("格林童话", "格林兄弟", new BigDecimal("36.00"), 150, "children", "德国经典童话集");
            createBook("小熊维尼", "A.A.米尔恩", new BigDecimal("24.50"), 140, "children", "经典儿童文学形象");
            createBook("长袜子皮皮", "阿斯特丽德·林格伦", new BigDecimal("22.00"), 120, "children", "瑞典儿童文学经典");
            createBook("神奇校车", "乔安娜·柯尔", new BigDecimal("68.00"), 90, "children", "科普知识绘本系列");
            createBook("了不起的狐狸爸爸", "罗尔德·达尔", new BigDecimal("20.00"), 100, "children", "幽默冒险儿童小说");
            createBook("纳尼亚传奇", "C.S.刘易斯", new BigDecimal("158.00"), 80, "children", "奇幻冒险系列小说");
            createBook("柳林风声", "肯尼思·格雷厄姆", new BigDecimal("25.00"), 110, "children", "经典儿童文学作品");
        }
    }
    
    // 创建图书的辅助方法
    private void createBook(String title, String author, BigDecimal price, int stock, String category, String description) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setStock(stock);
        book.setCategory(category);
        book.setDescription(description);
        bookMapper.save(book);
    }
}