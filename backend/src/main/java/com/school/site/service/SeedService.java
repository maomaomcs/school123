package com.school.site.service;

import com.school.site.entity.*;
import com.school.site.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 首次启动(空库)时填充成都市石室联中132学校的内容。
 * 文字资料来自公开网络(百度百科/成都本地宝/官方通稿)整理,已尽量核实;
 * 师资为示例条目,可在后台替换。图片:school-real.jpg 为公开校图,其余为书院风自制图。
 */
@Component
public class SeedService implements CommandLineRunner {

    private final ArticleRepository articleRepo;
    private final BannerRepository bannerRepo;
    private final TeacherRepository teacherRepo;
    private final SinglePageRepository pageRepo;

    public SeedService(ArticleRepository articleRepo, BannerRepository bannerRepo,
                       TeacherRepository teacherRepo, SinglePageRepository pageRepo) {
        this.articleRepo = articleRepo;
        this.bannerRepo = bannerRepo;
        this.teacherRepo = teacherRepo;
        this.pageRepo = pageRepo;
    }

    @Override
    public void run(String... args) {
        seedBanners();
        seedPages();
        seedTeachers();
        seedArticles();
    }

    private void seedBanners() {
        if (bannerRepo.count() > 0) return;
        bannerRepo.save(banner("成都市石室联中132学校", "石室文脉 · 航空科创 · 五育并举", "/img/school-real.jpg", 1));
        bannerRepo.save(banner("千年石室 弦歌不辍", "传承石室文脉 · 校训「爱国利民」", "/img/banner1.svg", 2));
        bannerRepo.save(banner("航空科创 国防教育", "全国航空教育特色学校 · 全国青少年校园足球特色学校", "/img/banner3.svg", 3));
    }

    private Banner banner(String title, String sub, String img, int sort) {
        Banner b = new Banner();
        b.setTitle(title);
        b.setSubtitle(sub);
        b.setImageUrl(img);
        b.setSort(sort);
        b.setEnabled(true);
        return b;
    }

    private void seedPages() {
        upsertPage("intro", "学校简介",
                "<p>成都市石室联中132学校，是由成都市青羊区教育局主管的公办九年一贯制学校（正筹备升级为十二年一贯制），隶属成都市石室联合中学教育集团。学校创办于2022年9月，2025年7月，经青羊区相关会议审议批准，由“成都市石室联合成飞学校”正式更名为“成都市石室联中132学校”。</p>"
                        + "<p>学校前身为成飞小学与成飞中学初中部，办学历史可追溯至1960年创建的成飞子弟学校，2006年整体移交青羊区政府管理。学校校址位于成都市青羊区黄田坝纬四路235号（经一路121号），占地约78亩，毗邻中国重要歼击机研制生产基地——航空工业成都飞机工业（集团）有限责任公司，校名中的“132”即源自成飞集团代号。</p>"
                        + "<p>学校现有教学班约60个，在校学生2600余名，教职员工200余人。依托石室联中教育集团与成飞集团的双重优质资源，学校以科技体育为特色，构建涵盖航空科技、国防教育的“航天课程”群，系全国国防教育示范学校，先后荣获全国青少年校园足球特色学校、全国航空教育特色学校等称号，着力打造“贯通教育 + 航空科创”的鲜明办学特色。</p>");
        upsertPage("principal", "校长寄语",
                "<p>亲爱的同学们、老师们、家长朋友们：</p>"
                        + "<p>欢迎走进成都市石室联中132学校。我们既承石室两千年弦歌不辍的文脉，秉“爱国利民”之校训；又续成飞人航空报国、自主创新的精神，以“132”为荣、以科创为志。</p>"
                        + "<p>在这里，我们希望每一位学子既能在书山学海中求真求实、涵养品格，也能在航空科创、体育艺术中放飞梦想、全面发展，成长为心怀家国、勇于担当的时代新人。</p>"
                        + "<p style=\"text-align:right\">—— 成都市石室联中132学校</p>");
        upsertPage("campus", "校园环境",
                "<p>学校占地约78亩，坐落于青羊区黄田坝片区，毗邻航空工业成飞集团，区位独特、底蕴深厚。校园硬件整体现代化，教学楼、实验室、图书馆、运动场、科创与航空教育空间一应俱全，为师生提供优质的学习与成长环境。</p>"
                        + "<p><img src=\"/img/school-real.jpg\" style=\"max-width:100%;border-radius:8px\" /></p>"
                        + "<p>依托石室联中教育集团“一校四区”的发展格局与集团化办学优势，学校持续推进校园文化和环境建设，营造安全、和谐、育人的校园氛围。</p>");
        upsertPage("contact", "联系我们",
                "<p><strong>学校名称：</strong>成都市石室联中132学校</p>"
                        + "<p><strong>办学性质：</strong>公办九年一贯制学校（青羊区教育局主管）</p>"
                        + "<p><strong>学校地址：</strong>四川省成都市青羊区黄田坝纬四路235号（经一路121号）</p>"
                        + "<p><strong>咨询电话：</strong>028-XXXXXXXX（请在后台“单页管理”中填写学校对外公布的招生/办公电话）</p>"
                        + "<p><strong>办公时间：</strong>周一至周五 8:30 - 17:30</p>"
                        + "<p>如有招生、教学、合作等事宜，欢迎通过下方留言表单与我们联系。</p>");
    }

    private void upsertPage(String key, String title, String content) {
        if (pageRepo.findById(key).isPresent()) return;
        SinglePage p = new SinglePage();
        p.setPageKey(key);
        p.setTitle(title);
        p.setContent(content);
        pageRepo.save(p);
    }

    private void seedTeachers() {
        if (teacherRepo.count() > 0) return;
        // 示例师资条目,请在后台“师资队伍”中替换为学校真实教师信息
        teacherRepo.save(teacher("张老师", "高级教师 · 学科带头人", "语文", 1, "（示例)深耕语文教学,注重人文素养与经典诵读。"));
        teacherRepo.save(teacher("李老师", "高级教师", "数学", 2, "（示例)倡导启发式教学,培养学生数理思维。"));
        teacherRepo.save(teacher("王老师", "一级教师 · 航空科创辅导", "科学/科创", 3, "（示例)负责航空科技与科创课程,指导学生参加各类科创赛事。"));
        teacherRepo.save(teacher("刘老师", "一级教师 · 校园足球教练", "体育", 4, "（示例)带领校足球队,践行阳光体育与足球特色。"));
        teacherRepo.save(teacher("陈老师", "一级教师 · 优秀班主任", "英语", 5, "（示例)以德育人,深受学生与家长信赖。"));
        teacherRepo.save(teacher("赵老师", "高级教师", "物理", 6, "（示例)推行探究式实验教学,培养科学精神。"));
    }

    private Teacher teacher(String name, String title, String subject, int sort, String intro) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setTitle(title);
        t.setSubject(subject);
        t.setSort(sort);
        t.setIntro(intro);
        t.setPhoto("/img/avatar.svg");
        return t;
    }

    private void seedArticles() {
        if (articleRepo.count() > 0) return;
        int i = 0;
        add("xwzx", "成都市石室联合成飞学校正式更名为成都市石室联中132学校",
                "2025年7月，经青羊区相关会议审议批准，学校由“成都市石室联合成飞学校”正式更名为“成都市石室联中132学校”。",
                "<p>2025年7月，经中共成都市青羊区委机构编制委员会相关会议议定审批，成都市石室联合成飞学校正式更名为“成都市石室联中132学校”。作为青羊区的老牌学校之一，学校依托石室联中教育集团与成飞集团双重优质资源，开启办学新篇章。</p><p>（内容据公开通稿整理，请在后台按学校正式表述校订并配图。）</p>", i++);
        add("xwzx", "石室联中132学校高中部9月开航 学校升级为十二年一贯制",
                "2026年，石室联中开办的第二所高中——石室联中132学校投用，学校将升级为一所十二年一贯制学校。",
                "<p>据公开报道，2026年石室联中开办的第二所高中石室联中132学校投用，该校属于石室联中的“一校四区”之一，此次开办高中后，学校升级为一所十二年一贯制学校，办学层次进一步提升。</p><p>（示例新闻，请在后台替换为学校正式发布的内容与照片。）</p>", i++);
        add("xwzx", "航空科创进校园 我校科技体育特色育人结硕果",
                "学校以科技体育为特色，构建航空科技、国防教育课程群，培养学生创新精神与实践能力。",
                "<p>学校以科技体育为特色，构建涵盖航空科技、国防教育的课程群，常态化开展科创发明、航模、机器人等活动，学生在各级各类科创与体育赛事中屡获佳绩。</p>", i++);

        add("tzgg", "关于2026年秋季学期校历及作息安排的通知", "现将本学期校历与作息安排公布如下，请知悉。",
                "<p>各年级、各班级：现将2026年秋季学期校历与作息安排公布如下，请遵照执行。（示例，请在后台按实际发布。）</p>", i++);
        add("tzgg", "关于开展校园安全大检查的通知", "为保障师生安全，学校将开展全面安全检查。",
                "<p>为营造安全和谐的校园环境，学校定于近期开展校园安全大检查，请各部门积极配合。（示例）</p>", i++);
        add("tzgg", "成都市石室联中132学校教师公开招聘公告", "诚邀优秀教育人才加入石室联中132学校。",
                "<p>因办学需要，学校面向社会公开招聘若干学科教师，欢迎有志于教育事业的优秀人才关注学校官方渠道发布的正式公告。（示例）</p>", i++);

        add("jyjx", "以研促教：我校开展集团联合教研活动", "依托石室联中教育集团，与集团学校保持教学管理同频共振。",
                "<p>学校依托石室联中教育集团“一校多区”的集团化办学优势，与集团学校开展联合教研，保持教育教学管理的同频共振，持续提升课堂质量。（示例）</p>", i++);
        add("jyjx", "航空科技特色课程：让课堂“飞”起来", "航空科技课程走进日常课堂，激发学生探究热情。",
                "<p>学校将航空科技元素融入日常课程，开设航模、无人机、国防教育等特色课程，让学生在动手实践中感受科技魅力。（示例）</p>", i++);

        add("dycd", "厚植家国情怀 传承航空报国精神", "学校开展国防教育与爱国主义主题活动。",
                "<p>学校结合航空科创特色，广泛开展国防教育与爱国主义主题活动，引导学生厚植家国情怀，传承航空报国精神。（示例）</p>", i++);
        add("dycd", "学雷锋志愿服务走进社区", "石室学子以行动践行“爱国利民”校训。",
                "<p>学校组织学生走进社区开展志愿服务，以实际行动践行“爱国利民”的石室校训。（示例）</p>", i++);

        add("xyfc", "校园足球联赛激情开赛 绿茵场上展风采", "全国青少年校园足球特色学校，绿茵场上活力四射。",
                "<p>作为全国青少年校园足球特色学校，我校校园足球联赛激情开赛，同学们在绿茵场上奋勇拼搏，展现青春风采。（示例）</p>", i++);
        add("xyfc", "科技节暨航空文化节精彩纷呈", "航模展演、科创作品秀，校园洋溢科创氛围。",
                "<p>学校科技节暨航空文化节精彩上演，航模展演、科创作品展、国防教育体验等活动轮番登场，校园洋溢浓浓科创氛围。（示例）</p>", i++);

        add("zsks", "成都市石室联中132学校招生咨询指南", "招生范围、报名流程、常见问题，请以官方发布为准。",
                "<p>学校招生相关信息请以青羊区教育局及学校官方渠道发布的正式公告为准。本页为示例，请在后台按当年招生政策更新。</p>", i++);
        add("zsks", "校园开放日邀请函", "诚邀家长与学子走进132，感受石室文脉与航空科创。",
                "<p>学校将举办校园开放日活动，欢迎广大家长和学生到校参观了解办学特色。（示例，具体时间以学校通知为准。）</p>", i++);
    }

    private void add(String category, String title, String summary, String content, int idx) {
        Article a = new Article();
        a.setCategory(category);
        a.setTitle(title);
        a.setSummary(summary);
        a.setContent(content);
        a.setAuthor("校办");
        a.setSource("成都市石室联中132学校");
        a.setCover(idx <= 1 ? "/img/school-real.jpg" : "/img/news-default.svg");
        a.setPublished(true);
        a.setTop(idx == 0);
        LocalDateTime t = LocalDateTime.now().minusDays(idx).minusHours(idx);
        a.setCreatedAt(t);
        a.setPublishedAt(t);
        articleRepo.save(a);
    }
}
