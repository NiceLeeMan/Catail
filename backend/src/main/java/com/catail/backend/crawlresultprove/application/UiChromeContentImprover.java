package com.catail.backend.crawlresultprove.application;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 삭제 대상 문구는 210건의 실제 크롤링 결과를 줄 단위로 쪼개 서로 다른 몇 개의 기사에 걸쳐
 * 반복되는지(cross-document frequency) 집계해 선정했다 — 실제 기사 본문 문장은 서로 다른 기사
 * 사이에 그대로 반복될 확률이 거의 없지만, 로그인/공유/댓글/카테고리 메뉴 등 사이트 UI 문구는
 * 기사마다 동일하게 반복되므로 반복 빈도로 UI 요소를 식별할 수 있다. "많이 본 뉴스", "관련기사"
 * 같은 다른 기사 헤드라인을 노출하는 위젯은 문구 자체가 매번 달라 이 방식으로 식별할 수 없으므로
 * 4단계(본문 외 영역 제거) 범주로 남겨둔다.
 */
public class UiChromeContentImprover implements ContentImprover {

    private static final Set<String> UI_CHROME_LINES = Set.of(
            "로그인", "회원가입", "로그인 회원가입", "로그인/회원가입", "회원로그인",
            "비밀번호", "닉네임", "아이디", "이름", "이름 비밀번호",
            "공유", "공유하기", "이 기사를 공유합니다", "기사공유하기", "SNS", "SNS 기사보내기",
            "페이스북", "facebook", "트위터", "twitter", "X", "엑스", "인스타그램", "instagram",
            "카카오톡", "네이버블로그", "네이버카페", "밴드", "youtube",
            "URL복사", "URL 복사", "URL주소", "URL이 복사되었습니다.",
            "댓글", "댓글쓰기", "댓글수정", "댓글삭제", "댓글닫기",
            "그래도 삭제하시겠습니까?", "삭제한 댓글은 다시 복구할 수 없습니다.",
            "BEST댓글", "BEST 댓글", "최신순 추천순 답글순", "내 댓글 모음", "댓글 정렬",
            "댓글 수정은 작성 후 1분내에만 가능합니다.", "0개의 댓글", "네티즌 의견 총 0 개",
            "자동등록방지", "스팸방지", "사이트 관리 규정에 어긋나는 의견글은 예고없이 삭제될 수 있습니다.",
            "댓글을 남기실 수 있습니다.", "그림의 영문, 숫자를 입력하세요.",
            "글자크기", "글자크기 설정", "글씨크기 크게", "글씨크기 작게", "글씨키우기",
            "본문글씨 줄이기", "본문글씨 키우기", "매우 작은 폰트", "매우 큰 폰트",
            "큰 폰트", "작은 폰트", "보통 폰트", "본문 글자 크기 조정",
            "검색", "검색창 닫기", "검색창 열기 메뉴", "본문 바로가기", "본문으로 바로가기",
            "주메뉴 바로가기", "위로", "최상단으로", "ESC 닫기", "닫기", "확인", "이전", "다음",
            "이전 다음", "Prev Next", "목록", "스크롤 이동 상태바",
            "홈", "전체메뉴", "전체메뉴닫기", "전체메뉴 닫기", "전체메뉴 버튼", "전체 메뉴",
            "뒤로 멈춤 앞으로", "화면 상단으로 이동",
            "검색어를 입력해주세요. 검색하기", "기사검색 검색", "통합 검색 삭제 검색",
            "개인정보처리방침", "개인정보취급방침", "개인정보 처리방침", "이용약관",
            "청소년보호정책", "이메일무단수집거부", "저작권보호정책", "저작권규약",
            "윤리강령", "윤리헌장", "광고문의", "광고안내", "사업제휴", "인재채용", "채용",
            "Powered by", "Powered by Translate", "RSS", "모바일버전", "모바일웹", "사이트맵",
            "신문사소개", "회사소개", "매체소개", "서비스안내", "고충처리", "독자게시판",
            "독자 불만 처리", "1:1 문의", "서비스 문의", "자주 묻는 질문",
            "Promoted Links Promoted Links", "Sponsored Sponsored", "Advertisements"
    );

    private static final Set<String> CATEGORY_MENU_WORDS = Set.of(
            "오피니언", "경제", "산업", "사회", "금융", "자동차", "정치", "부동산", "국제", "스포츠", "문화",
            "골프", "유럽", "증권", "연예", "교육", "영화", "인터뷰", "전국", "기획", "IT", "야구", "강원",
            "일본", "게임", "라이프", "인물", "유통", "은행", "칼럼", "북한", "세계", "글로벌", "중국",
            "청와대", "방송", "사설", "경기", "기아", "종교", "건강", "노동", "인천", "외교", "전북", "보험",
            "연재", "제주", "식음료", "기타", "중남미", "바이오", "반려동물", "울산", "국제기구", "해외연예",
            "재외동포", "전남광주", "중화학", "충북", "농구", "재테크", "가요", "경남", "생활", "국제경제",
            "부산", "철강", "공시", "배구", "국방", "다문화", "종합", "특파원", "호남", "남성", "통신",
            "채권", "화보", "헬스케어", "여성", "시승기", "전자", "여행", "ESG", "국회", "부울경", "환경",
            "대중문화", "음악", "서울", "충청", "미디어", "기업", "인사", "책", "영상", "동영상", "그래픽",
            "만평", "이벤트", "키워드", "피플", "사람", "뉴스", "전체", "부고",
            "건설", "건설/부동산", "건설·부동산", "농구/배구", "해외야구", "해외주식", "패션",
            "공연/전시", "취업/창업", "법원/검찰", "팩트체크", "중동/아프리카", "미국/북미", "아시아/호주",
            "아시아·호주", "여행/레저", "해양수산", "대전/충남/세종", "패션·뷰티", "책/문학", "여성/아동",
            "청와대/총리실", "에너지/자원", "학술/문화재", "경제/정책", "농림축산", "학술/연구",
            "복지/노동", "만화/웹툰", "국내주식", "기후/환경", "대구/경북", "대구&경북", "국회/정당",
            "국회·정당", "사건/사고", "사건사고", "사건·사고", "산업/기업", "유통/서비스", "인사/동정",
            "동정", "동정/게시판", "증권/운용사", "금융·증권", "금융/증권", "펀드/ETF", "생활경제", "생활·문화",
            "경제일반", "정치일반", "사회일반", "국제일반", "문화일반", "금융일반", "IT일반", "IT/과학",
            "IT·과학", "산업일반", "국제 일반", "경제 일반", "정치 일반", "문화 일반", "사회 일반", "과학일반",
            "연예전체", "사회전체", "부동산일반", "전국전체", "경제전체", "문화전체", "마켓+전체",
            "스포츠전체", "세계전체", "정치전체", "사람들전체", "건강전체", "북한전체", "산업전체",
            "전체기사", "전체기사보기", "전체뉴스", "최신뉴스", "최신기사", "뉴스레터", "미래 모빌리티"
    );

    private static final Pattern MENU_TOGGLE = Pattern.compile(".{0,10}메뉴\\s*열기/닫기");
    private static final Pattern PAGINATION_NUMBER = Pattern.compile("\\d{1,3}");
    private static final Pattern COUNT_BADGE = Pattern.compile("(댓글|좋아요)\\s*\\d+");

    private final ImagePlaceholderContentImprover imagePlaceholderContentImprover = new ImagePlaceholderContentImprover();
    private final BasicNormalizationContentImprover basicNormalizationContentImprover = new BasicNormalizationContentImprover();

    @Override
    public String improve(String content) {
        String preprocessed = imagePlaceholderContentImprover.improve(content);

        StringBuilder result = new StringBuilder();
        for (String line : preprocessed.split("\n", -1)) {
            if (isUiChrome(line.strip())) {
                continue;
            }
            result.append(line).append('\n');
        }

        return basicNormalizationContentImprover.improve(result.toString());
    }

    private boolean isUiChrome(String line) {
        if (line.isEmpty()) {
            return false;
        }
        return UI_CHROME_LINES.contains(line)
                || CATEGORY_MENU_WORDS.contains(line)
                || MENU_TOGGLE.matcher(line).matches()
                || PAGINATION_NUMBER.matcher(line).matches()
                || COUNT_BADGE.matcher(line).matches();
    }
}
