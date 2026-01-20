one-more-recipe-fe/
├── public/                 # 정적 자원 (favicon, 로고 등)
├── src/                    # 실제 소스 코드 영역
│   ├── api/                # [Network] 통신 관련 로직 (Axios 설정 등)
│   ├── assets/             # [Static] 이미지, 폰트, 전역 CSS 파일
│   ├── components/         # [UI Unit] 재사용 가능한 공통 UI 부품들
│   ├── hooks/              # [Logic] 비즈니스 로직 및 상태 관리 (Custom Hooks)
│   ├── pages/              # [View] 화면 단위의 최상위 컴포넌트
│   ├── types/              # [Type] TypeScript 인터페이스 및 타입 정의
│   ├── utils/              # [Helper] 공통으로 사용되는 유틸리티 함수
│   ├── App.tsx             # [Root] 메인 라우팅 및 전체 구조 정의
│   └── main.tsx            # [Entry] 프로젝트 시작점 (렌더링 시작)
├── .env                    # [Config] 환경 변수 (Spring 서버 주소 등)
├── package.json            # [Manifest] 라이브러리 관리 및 프로젝트 정보
└── tsconfig.json           # [Config] TypeScript 컴파일러 설정