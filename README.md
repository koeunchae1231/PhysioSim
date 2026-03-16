# PhysioSim
인체생리학에 의거한 자바 기반 프로젝트

## 소개
- 계층 구조: 세포 → 조직 → 기관 → 기관계 → 개체(sim)
- 목표: 하나의 공통 시뮬레이션 코어 위에서  
- 개발 환경: Eclipse + JDK 17 + SQLite (JDBC)

## 주요 기능
1. 사용자 입력(성별, 키, 체중)
   > DB에 저장해 캐릭터 파라미터 초기화
2. 생리학적 상호작용
   > 신경계, 심혈관계, 근육계, 내분비계, 호흡계, 비뇨계, 소화계, 감각계, 생식계
3. 시각화
   - 실시간 그래프/수치/알람, 개입 패널(O₂, 출혈/수액 등) + 캐릭터 모션(호흡/피부색/아이콘), 핵심 바이탈 카드(HR, RR, SpO₂, MAP)

## 설계 개요
- 패키지
  - `physiosim.core`: `Updatable`, `EventBus`, `SimulationClock`, `Units/Quantity`
  - `physiosim.cell / tissue / organ / system`: 생리 도메인 계층
  - `physiosim.sim`: `Character`, `VitalSigns`, `HomeostasisController`
  - `physiosim.control`: `Scenario`, `Intervention`, `AlarmManager`, Recorder/Playback
  - `physiosim.db`: `Database`, `UserRepository`, `CharacterRepository`, `VitalRepository` …
  - `physiosim.ui`: ModeSelect / Login / PatientView / ClinicianView
  - `physiosim.event`: 알람/개입/상태 변화 이벤트 타입

## 진행 기록
### DAY 1
- GitHub Repo 생성
- README.md 초안 작성
- 프로젝트 기본 구조 설계 시작 및 추가 정보 학습
- 패키지 추가

### DAY 2
- SQLite 연동 및 DB 초기화
- 사용자 기본 정보(성별, 키, 체중) 저장 기능 구현

### DAY 3
- 로그인/회원가입 기능 추가 (백엔드 로직)
- 비밀번호 로직 추가
- 구조 재정립: 계정 설정의 이유 (사용자의 계정 - 다 캐릭터 - 캐릭터의 바이탈)

### DAY 4
- 유저, 캐릭터, 바이탈의 레포지토리 리팩토링
- Database 스키마 일부 제약/인덱스 수정 및 보완
- 로그인/회원가입 UI 틀 만들기 (SWING - AWT 기반 확장 라이브러리)

### DAY 5
- 스플래시 뷰, 메인 프레임, 홈 뷰 디자인 완성
- 회원가입 뷰와 로그인 뷰 틀 만들기
- 전반적인 흐름에 따른 디자인 구상 완성

### DAY 6
- theme로 디자인 통일
- 개인 뷰, 캐릭터 생성 뷰, 계정 설정 뷰, 캐릭터 리스트 뷰 틀 만들기

### DAY 7
- 도트 캐릭터 제작 및 바이탈 뷰 계산 설계
- 인터페이스 정리

### DAY 8
- 바이탈 뷰 및 캐릭터 뷰 제작

### DAY 9
- 바이탈 뷰 수치 입력 오류 제어

### DAY 10
- 캐릭터 뷰 로그 제어

## 메모
단위 고정: bpm / % / mmHg / ℃ / mg·dL
MAP 계산식: MAP = DBP + (SBP - DBP)/3

DB 부트스트랩(통일 규칙)
Database.open(path): 연결만 연다.
Database.setup(connection): PRAGMA + 스키마 생성(멱등) 을 수행한다.
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA temp_store = MEMORY;
PRAGMA busy_timeout = 5000;
// PRAGMA wal_autocheckpoint = 1000; / PRAGMA cache_size = -20000;
