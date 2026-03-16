# PhysioSim
Java-based physiology simulation platform for modeling and visualizing vital signs.

인체 생리학 개념을 기반으로 한 자바 기반 시뮬레이션 프로젝트이다.  
생리 신호를 모델링하고, 시각화 인터페이스를 통해 인체 상태 변화를 관찰할 수 있도록 설계하였다.

---

## 향후 개발 방향

PhysioSim은 이후 개발될 **VitaCore 플랫폼의 프로토타입** 역할을 한다.

향후에는 다음과 같은 방향으로 확장할 예정이다.

- 웹 기반 플랫폼 구조로 확장
- 생리 시뮬레이션 모델 확장
- 실시간 데이터 연동
- 인터페이스 및 시스템 구조 개선

---

## 현재 구현 범위

PhysioSim currently focuses on **vital-level physiological simulation**.  
바이탈 신호의 변화를 시뮬레이션하고, 캐릭터 상태 변화와 대시보드를 통해 이를 시각적으로 표현한다.

현재 구현된 기능은 다음과 같다.

- 바이탈 대시보드 시각화
- 명령어 기반 생리 이벤트 시스템
- 캐릭터 상태 변화
- SQLite 기반 사용자 / 캐릭터 / 바이탈 데이터 관리

대표 이벤트 예시

- BLEED → hypotension
- COLD → hypothermia
- HOT → fever
- INFUSE → hypertension

---

## 소개

- 계층 구조  
  **세포 → 조직 → 기관 → 기관계 → 개체(sim)**

- 목표  
  하나의 공통 시뮬레이션 코어 위에서 다양한 생리 시스템이 상호작용하도록 구성

- 개발 환경  
  **Eclipse / JDK 17 / SQLite (JDBC)**

---

## 주요 기능

### 1. 사용자 입력 기반 캐릭터 생성
- 성별, 키, 체중 입력
- DB에 저장된 정보를 기반으로 캐릭터의 기본 파라미터 초기화

### 2. 생리학적 상호작용 모델
- 생리 이벤트 기반 시뮬레이션
- 각 이벤트는 바이탈 변화와 캐릭터 상태를 동시에 변화시킨다.

### 3. 시각화 인터페이스

- 실시간 그래프 및 수치 표시
- 상태 알람 시스템
- 개입 패널 (산소 공급, 출혈, 수액 등)
- 캐릭터 상태 표현
  - 호흡 변화
  - 피부색 변화
  - 상태 아이콘

핵심 바이탈 카드

- HR (Heart Rate)
- RR (Respiratory Rate)
- SpO₂ (Oxygen Saturation)
- MAP (Mean Arterial Pressure)

---

## 설계 개요

### 패키지 구조

```
physiosim.sim
 ├ Cell
 ├ Tissue
 ├ Core
 ├ EventConsumer
 ├ Organ
 ├ OrganSystem
 ├ Snapshot
 └ SpriteState

physiosim.control
 └ Simulation

physiosim.db
 ├ Database
 ├ UserRepository
 ├ CharacterRepository
 ├ VitalRepository
 └ Passwords

physiosim.ui
 ├ App
 ├ Navigator
 ├ Theme
 └ views
     ├ SplashView
     ├ HomeView
     ├ SignupView
     ├ LoginView
     ├ MainView
     ├ PersonalView
     ├ CharacterCreateView
     ├ AccountView
     ├ ListView
     ├ VitalView
     └ CharacterView

physiosim.event
 ├ Command
 ├ CommandDirection
 ├ CommandId
 ├ CommandMapper
 ├ PhysioEvent
 └ TargetSystem
```

## Database Bootstrapping
### Database 초기화 규칙
PhysioSim은 SQLite 데이터베이스를 사용하며, Database 클래스가 로딩될 때 자동으로 데이터베이스 초기화를 수행한다.

Database.getConnection()
- SQLite 데이터베이스(data/physiosim.db)에 연결한다.

Database.init()
- 클래스 로딩 시 자동 실행되며 데이터베이스 스키마를 초기화한다.

스키마 생성 (멱등 처리)
- CREATE TABLE IF NOT EXISTS 구문을 사용하여 테이블이 존재하지 않을 경우에만 생성한다.

현재 생성되는 테이블은 다음과 같다.
- users
- characters

이 방식은 다음과 같은 특징을 가진다.
- 애플리케이션 시작 시 자동 초기화
- 기존 데이터 유지
- 스키마 중복 생성 방지

---

## Interface

### Splash Screen
![splash](images/splash.png)

### Home Screen
![home](images/home.png)

### Login Screen
![login](images/login.png)

### Vital Dashboard
![vital](images/vital.png)

### Character Normal
![character](images/character-normal.png)

### Character Fever
![character](images/character-fever.png)

### Command System
![command](images/character-command.png)

---

## Devlog

### Day 1
- GitHub Repository 생성
- README 초안 작성
- 프로젝트 구조 설계
- 패키지 구성

### Day 2
- SQLite 연동
- DB 초기화 로직 구현
- 사용자 기본 정보 저장 기능 구현

### Day 3
- 로그인 / 회원가입 기능 구현
- 비밀번호 처리 로직 추가
- 데이터 구조 재정립  
  (사용자 계정 → 다중 캐릭터 → 캐릭터별 바이탈 데이터)

### Day 4
- Repository 구조 리팩토링
- Database 스키마 제약 및 인덱스 수정
- 로그인 / 회원가입 UI 기본 틀 구현

### Day 5
- Splash / MainFrame / HomeView 디자인 완성
- 로그인 / 회원가입 화면 구성
- 전체 UI 흐름 정리

### Day 6
- Theme 기반 UI 디자인 통일
- 개인 뷰 / 캐릭터 생성 / 계정 설정 / 캐릭터 리스트 화면 구성

### Day 7
- 도트 캐릭터 제작
- 바이탈 뷰 계산 로직 설계
- 인터페이스 구조 정리

### Day 8
- Vital View 구현
- Character View 구현

### Day 9
- Vital View 입력 오류 처리

### Day 10
- Character View 로그 제어 기능 추가

---

## Technical Notes

단위
- bpm
- %
- mmHg
- ℃
- mg·dL

MAP 계산식
- MAP = DBP + (SBP - DBP) / 3
