-- PostgreSQL版本数据库初始化脚本
-- 适用于Render PostgreSQL

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  address VARCHAR(200),
  balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  role INT DEFAULT 0 
);

-- 图书表
CREATE TABLE IF NOT EXISTS books (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  category VARCHAR(50),
  description TEXT
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  order_number VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL,
  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10,2) NOT NULL,
  status INT DEFAULT 0, 
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 钱包表
CREATE TABLE IF NOT EXISTS wallet (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 钱包交易记录表
CREATE TABLE IF NOT EXISTS wallet_transaction (
  id BIGSERIAL PRIMARY KEY,
  wallet_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  type VARCHAR(20) NOT NULL, 
  description VARCHAR(255),
  related_order_id BIGINT,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', 
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (wallet_id) REFERENCES wallet(id),
  FOREIGN KEY (related_order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- 购物车项表
CREATE TABLE IF NOT EXISTS cart_items (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 初始化数据脚本

-- 初始化管理员数据
INSERT INTO admin (username, password, email, phone, create_time, update_time) VALUES
('admin', '123456', 'admin@example.com', '13800138000', NOW(), NOW());

-- 初始化普通用户数据
INSERT INTO users (username, password, email, role) VALUES
('user', '123456', 'user@example.com', 0);

-- 初始化图书数据
-- 文学小说类 - novel
INSERT INTO books (title, author, price, stock, category, description) VALUES
('三体', '刘慈欣', 23.00, 100, 'novel', '刘慈欣代表作，亚洲首部获得雨果奖的科幻巨著'),
('活着', '余华', 20.00, 200, 'novel', '余华代表作，讲述了人如何活着的故事'),
('百年孤独', '加西亚·马尔克斯', 35.50, 120, 'novel', '魔幻现实主义文学的代表作'),
('围城', '钱钟书', 28.00, 150, 'novel', '中国现代文学经典，描写知识分子的生活'),
('平凡的世界', '路遥', 68.00, 180, 'novel', '中国当代文学经典，反映改革开放初期农村生活'),
('红楼梦', '曹雪芹', 42.00, 130, 'novel', '中国古典四大名著之一'),
('西游记', '吴承恩', 30.00, 140, 'novel', '中国古典四大名著之一'),
('三国演义', '罗贯中', 35.00, 120, 'novel', '中国古典四大名著之一'),
('水浒传', '施耐庵', 36.00, 110, 'novel', '中国古典四大名著之一'),
('悲惨世界', '维克多·雨果', 45.00, 90, 'novel', '法国文学巨著，讲述善与恶的故事'),
('追风筝的人', '卡勒德·胡赛尼', 29.80, 160, 'novel', '阿富汗背景下的救赎故事'),
('挪威的森林', '村上春树', 27.80, 170, 'novel', '村上春树代表作，青春与爱情的故事'),
('时间简史', '斯蒂芬·霍金', 38.00, 80, 'novel', '探索宇宙奥秘的科普名著'),
('月亮与六便士', '毛姆', 25.00, 130, 'novel', '关于理想与现实的经典小说');

-- 科技计算机类 - technology
INSERT INTO books (title, author, price, stock, category, description) VALUES
('JavaScript高级程序设计', 'Nicholas C. Zakas', 99.00, 50, 'technology', 'JavaScript领域经典图书，入门与提高必读'),
('深入理解计算机系统', 'Randal E. Bryant', 115.00, 40, 'technology', '计算机系统底层原理解析，程序员必读书籍'),
('算法导论', 'Thomas H. Cormen', 128.00, 30, 'technology', '算法领域的圣经级教材'),
('代码大全', 'Steve McConnell', 108.00, 60, 'technology', '软件构建实践指南，编程技巧全书'),
('人工智能：一种现代方法', 'Stuart Russell', 145.00, 35, 'technology', 'AI领域经典教材，全面介绍人工智能技术'),
('Spring实战', 'Craig Walls', 89.00, 70, 'technology', 'Spring框架实战指南'),
('Python编程：从入门到实践', 'Eric Matthes', 79.00, 90, 'technology', 'Python学习入门经典教程'),
('图解HTTP', '上野宣', 49.00, 80, 'technology', '通俗易懂的HTTP协议入门书'),
('Java核心技术', 'Cay S. Horstmann', 111.00, 60, 'technology', 'Java技术的权威指南'),
('设计模式', 'Erich Gamma', 78.00, 65, 'technology', '软件设计模式的开山之作'),
('编程珠玑', 'Jon Bentley', 59.00, 75, 'technology', '编程技巧与思想的经典'),
('数据结构与算法分析', 'Mark Allen Weiss', 85.00, 50, 'technology', '数据结构与算法学习指南'),
('机器学习', '周志华', 88.00, 40, 'technology', '机器学习入门经典教材'),
('Clean Code', 'Robert C. Martin', 69.00, 55, 'technology', '编写整洁代码的艺术');

-- 社会科学类 - education
INSERT INTO books (title, author, price, stock, category, description) VALUES
('教育心理学', '安妮·伍尔福克', 68.00, 60, 'education', '教育心理学经典教材'),
('乌合之众', '古斯塔夫·勒庞', 32.00, 100, 'education', '群体心理研究的经典之作'),
('社会学概论', '安东尼·吉登斯', 78.00, 75, 'education', '社会学入门教材'),
('人类简史', '尤瓦尔·赫拉利', 68.00, 120, 'education', '从人类进化角度看世界历史'),
('思考，快与慢', '丹尼尔·卡尼曼', 59.50, 90, 'education', '行为经济学经典著作'),
('沟通的艺术', '罗纳德·阿德勒', 45.00, 80, 'education', '人际沟通技巧指南'),
('影响力', '罗伯特·西奥迪尼', 42.00, 85, 'education', '心理学经典著作，关于说服与影响力'),
('社会契约论', '卢梭', 28.00, 70, 'education', '政治哲学经典著作'),
('教育学原理', '王道俊', 45.00, 60, 'education', '教育学基础理论教材'),
('批判性思维', '理查德·保罗', 39.00, 65, 'education', '培养批判性思维能力的指南'),
('心理学与生活', '理查德·格里格', 88.00, 50, 'education', '心理学入门经典教材'),
('哲学导论', '尼古拉斯·沃尔特斯托夫', 52.00, 55, 'education', '西方哲学入门读物'),
('逻辑学导论', '欧文·科皮', 48.00, 45, 'education', '逻辑学基础教材'),
('公正：该如何做是好？', '迈克尔·桑德尔', 38.00, 95, 'education', '哈佛大学公开课基础读物');

-- 经济管理类 - economics
INSERT INTO books (title, author, price, stock, category, description) VALUES
('国富论', '亚当·斯密', 65.00, 50, 'economics', '经济学奠基之作'),
('经济学原理', '曼昆', 88.00, 70, 'economics', '经济学入门经典教材'),
('怪诞行为学', '丹·艾瑞里', 42.00, 80, 'economics', '行为经济学经典著作'),
('金融学', '兹维·博迪', 95.00, 40, 'economics', '金融学基础教材'),
('管理学', '斯蒂芬·罗宾斯', 78.00, 60, 'economics', '管理学经典教材'),
('从优秀到卓越', '吉姆·柯林斯', 45.00, 85, 'economics', '管理实践经典著作'),
('薪酬管理', '乔治·米尔科维奇', 68.00, 35, 'economics', '人力资源管理专业教材'),
('市场营销学', '菲利普·科特勒', 75.00, 50, 'economics', '市场营销学经典教材'),
('投资学', '威廉·夏普', 86.00, 45, 'economics', '投资理论与实践教材'),
('穷查理宝典', '查理·芒格', 58.00, 75, 'economics', '投资智慧集锦'),
('公司财务原理', '理查德·布雷利', 92.00, 30, 'economics', '公司财务管理教材'),
('战略管理', '迈克尔·波特', 78.00, 40, 'economics', '企业战略管理经典'),
('商业模式新生代', '亚历山大·奥斯特瓦德', 49.00, 65, 'economics', '商业模式设计与创新指南'),
('组织行为学', '斯蒂芬·罗宾斯', 72.00, 50, 'economics', '组织行为学经典教材');

-- 历史传记类 - history
INSERT INTO books (title, author, price, stock, category, description) VALUES
('万历十五年', '黄仁宇', 39.50, 70, 'history', '从细节透视明朝历史'),
('明朝那些事儿', '当年明月', 328.00, 80, 'history', '通俗明朝历史读物'),
('人类群星闪耀时', '茨威格', 36.00, 65, 'history', '历史转折点的人物群像'),
('第二次世界大战回忆录', '丘吉尔', 198.00, 40, 'history', '二战亲历者的历史记录'),
('拿破仑传', '埃米尔·路德维希', 58.00, 55, 'history', '拿破仑一世的传记作品'),
('史记', '司马迁', 125.00, 60, 'history', '中国第一部纪传体通史'),
('资治通鉴', '司马光', 158.00, 45, 'history', '中国古代编年体通史'),
('光荣与梦想', '威廉·曼彻斯特', 168.00, 35, 'history', '美国现代史1932-1972年'),
('枪炮、病菌与钢铁', '贾雷德·戴蒙德', 59.00, 75, 'history', '人类社会发展史'),
('希特勒传', '约阿希姆·费斯特', 88.00, 30, 'history', '客观记录纳粹德国领导人生平'),
('汉密尔顿传', '罗恩·彻诺', 68.00, 50, 'history', '美国开国元勋的传记'),
('中国通史', '吕思勉', 78.00, 60, 'history', '中国历史通俗读本'),
('世界简史', '威尔斯', 55.00, 70, 'history', '世界历史概览'),
('中国历代政治得失', '钱穆', 38.00, 80, 'history', '中国政治制度史研究');

-- 艺术设计类 - art
INSERT INTO books (title, author, price, stock, category, description) VALUES
('写给大家看的设计书', 'Robin Williams', 59.00, 70, 'art', '平面设计入门经典'),
('艺术的故事', '贡布里希', 128.00, 50, 'art', '西方艺术史经典著作'),
('设计心理学', '唐纳德·诺曼', 65.00, 60, 'art', '设计与人类心理的关系'),
('配色设计原理', '佩妮·伯克', 78.00, 55, 'art', '色彩搭配与应用指南'),
('摄影的艺术', '布鲁斯·巴纳姆', 85.00, 45, 'art', '摄影技巧与艺术表现'),
('素描的诀窍', '伯特·多德森', 48.00, 65, 'art', '素描入门基础教程'),
('版式设计原理', '蒂莫西·萨马拉', 75.00, 40, 'art', '版面设计与排版指南'),
('中国美术史', '中央美术学院', 98.00, 30, 'art', '中国美术发展历程'),
('色彩心理学', '伊顿', 56.00, 50, 'art', '色彩对人类心理的影响研究'),
('建筑的意象', '凯文·林奇', 45.00, 60, 'art', '城市设计与建筑规划经典'),
('西方音乐简史', '保罗·亨利·朗', 68.00, 35, 'art', '西方音乐发展历程'),
('服装设计入门', '苏珊娜·亚西尔', 72.00, 45, 'art', '服装设计基础知识'),
('电影艺术', '大卫·波德维尔', 82.00, 30, 'art', '电影制作与鉴赏'),
('字体设计手册', '简·蒂格', 89.00, 40, 'art', '字体设计与应用指南');

-- 儿童读物类 - children
INSERT INTO books (title, author, price, stock, category, description) VALUES
('小王子', '安东尼·德·圣-埃克苏佩里', 22.00, 150, 'children', '经典儿童文学作品'),
('夏洛的网', 'E.B.怀特', 25.00, 120, 'children', '关于友谊的儿童文学名著'),
('窗边的小豆豆', '黑柳彻子', 26.50, 140, 'children', '日本儿童教育小说'),
('绿山墙的安妮', '露西·莫德·蒙格马利', 28.00, 110, 'children', '经典儿童成长小说'),
('哈利·波特与魔法石', 'J.K.罗琳', 32.50, 200, 'children', '魔法冒险系列小说'),
('爱的教育', '亚米契斯', 18.00, 130, 'children', '意大利儿童文学经典'),
('安徒生童话', '安徒生', 35.00, 160, 'children', '世界著名童话故事集'),
('格林童话', '格林兄弟', 36.00, 150, 'children', '德国经典童话集'),
('小熊维尼', 'A.A.米尔恩', 24.50, 140, 'children', '经典儿童文学形象'),
('长袜子皮皮', '阿斯特丽德·林格伦', 22.00, 120, 'children', '瑞典儿童文学经典'),
('神奇校车', '乔安娜·柯尔', 68.00, 90, 'children', '科普知识绘本系列'),
('了不起的狐狸爸爸', '罗尔德·达尔', 20.00, 100, 'children', '幽默冒险儿童小说'),
('纳尼亚传奇', 'C.S.刘易斯', 158.00, 80, 'children', '奇幻冒险系列小说'),
('柳林风声', '肯尼思·格雷厄姆', 25.00, 110, 'children', '经典儿童文学作品');