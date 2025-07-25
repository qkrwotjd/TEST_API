# TEST_API
# 1. 핵심 로직 구현 코드



# 2.설계 및 보안 아키텍처 기술서
  
## A. 시스템 아키텍처

### 기술스택

언어 : Java 

=> 웹 개발에서 가장 널리 사용되는 언어 중 하나이며, 지금까지 가장 많이 다뤄본 언어이기 때문에 선택했습니다.

프레임워크 : SpringBoot

=> 초기 설정이 간편하고, RESTful API를 효율적으로 개발할 수 있는 구조를 제공하여 API 서버 구현에 적합하다고 판단했습니다.

DB : Hibernate (JPA 기반 ORM 프레임워크) 

=> Java 객체(Entity 클래스)와 관계형 데이터베이스(PostgreSQL) 간의 매핑을 자동화하여 SQL 작성 없이도 CRUD 기능을 효율적으로 구현할 수 있었어서 선택했습니다.

### DB 스키마

```
- 회사 테이블
CREATE TABLE company (
    company_id VARCHAR PRIMARY KEY,
    company_name VARCHAR
);


- 카테고리 종류 테이블
CREATE TABLE category (
    seq_no SERIAL PRIMARY KEY,
    company_id VARCHAR,
    category_id VARCHAR,
    category_name VARCHAR,
    CONSTRAINT fk_category_company FOREIGN KEY (company_id) REFERENCES company (company_id)
);


- 카테고리들이 들고 있는 keyword 테이블
CREATE TABLE category_keyword (
    seq_no SERIAL PRIMARY KEY,
    keyword VARCHAR,
    category_seq_no INTEGER,
    CONSTRAINT fk_category_keyword_category FOREIGN KEY (category_seq_no) REFERENCES category (seq_no)
);


- 회사별 거래내역(카테고리, keyword 포함) 테이블
CREATE TABLE transaction_result (
    seq_no SERIAL PRIMARY KEY,
    company_id VARCHAR,
    category_id VARCHAR,
    company_name VARCHAR,
    category_name VARCHAR,
    keyword VARCHAR,
    deposit_amt BIGINT,
    withdraw_amt BIGINT,
    balance BIGINT,
    tr_dt TIMESTAMP,
    branch VARCHAR
);

```

## 핵심 자동 분류 로직

rules.json을 기반으로, 회사별 카테고리(category_id) 정보를 CATEGORY 테이블에 저장하고, 각 카테고리에 해당하는 키워드들은 CATEGORY_KEYWORD 테이블에 연동하여 저장하였습니다. 이후 거래내역(CSV) 파일을 한 줄씩 순회하며 적요 컬럼의 값을 기준으로 각 카테고리 키워드와 비교하여 자동 분류를 수행하였습니다.

º 키워드가 적요에 포함된 경우 : 해당 키워드가 속한 카테고리를 조회하여 적용

º 포함되지 않은 경우 : 미분류로 지정된 카테고리로 적용

이렇게 자동 분류된 결과를 TRANSACTION_RESULT 테이블에 저장하였습니다.

## 보안 강화 방안

공인인증서와 비밀번호 등 민감 정보를 다루는 시스템에서는 암호화와 접근 제어를 기본으로 고려해야 합니다. 먼저, 비밀번호나 주민등록번호, 계좌번호와 같은 민감정보는 DB에 저장할 때 AES 등의 대칭키 암호화 방식을 사용하여 암호화하고, 복호화 키는 환경 변수 또는 보안 서버에 별도로 분리하여 관리합니다.

공인인증서 파일은 DB에 직접 저장하지 않고, 서버 파일 시스템에 안전한 경로로 저장하며, 해당 경로 정보만을 DB에 저장합니다. 이후 파일에 접근할 수 있는 경로는 Controller 또는 Service 레이어를 통해서만 열람 가능하도록 제한하고, 사용자 인증 및 권한 검증 로직을 반드시 통과해야 접근할 수 있도록 구현합니다.

또한 파일 자체는 외부에서 직접 접근이 불가능한 디렉터리에 저장하고, 웹 서버가 직접 static하게 노출하지 않도록 설정합니다. 접근 시에는 다운로드 URL에 토큰이나 일회성 인증 정보를 활용할 수도 있습니다.
추가적으로, DB 접근 권한은 최소 권한 원칙에 따라 구성하며, 민감 정보에 접근할 수 있는 서비스 계정과 일반 조회 계정을 분리하여 운영합니다.

## 문제상황 해결책 제시

시나리오 : 한 고객사의 거래 데이터가 다른 고객사 대시보드에 노출되었다는 신고가 들어왔습니다.

### 즉시조치

가장 우선적으로 해야 할 일은 문제가 발생한 고객사의 조회 권한을 일시적으로 차단하는 것입니다.

정보가 더 유출되는 것을 방지하고, 악의적인 접근 가능성을 차단하기 위한 신속한 대응이 필요합니다.

### 원인 분석
로그 분석을 통해 어떤 API 또는 쿼리에서 조회가 발생했는지 추적합니다.

문제 쿼리를 식별한 뒤, 사용된 파라미터가 제대로 전달되었는지 확인합니다.

쿼리를 호출하는 백엔드 코드의 컨트롤러 및 서비스 계층에서 로직상 오류나 파라미터 누락 여부를 점검합니다.

문제가 쿼리가 아닌 권한 또는 페이지 노출 문제인 경우,
SecurityConfig 혹은 인증(Authentication)/인가(Authorization) 설정이 적절히 이루어졌는지를 검토합니다.

### 재발 방지 대책

고객사(사업장) 단위로 고유 코드를 부여하고, 모든 요청에 대해 이 코드를 포함시켜 서버 측에서 검증 로직을 추가합니다.
예를 들어, 사용자의 세션에 포함된 사업장 고유 코드와 요청 파라미터의사업장 고유 코드가 다르면 예외(Exception)를 발생시켜 접근을 차단합니다.

또한, 데이터 접근 시에는 반드시 로그인된 사용자 또는 토큰 정보 기준으로 필터링하는 보안 정책을 적용해야 합니다.


# 3. 실행 및 테스트 가이드 
Postman 사용

 ### 파일 업로드

 https://test-api-5v5e.onrender.com/api/v1/accounting/process 으로 접속하여 Json 파일 Key : jsonFile, Csv 파일 Key : csvFile로 설정
 
 - 예시

<img width="842" height="654" alt="image" src="https://github.com/user-attachments/assets/44c6ebc7-1d29-42f1-85a7-70fa858a9894" />

 ### 데이터 조회

 https://test-api-5v5e.onrender.com/api/v1/accounting/records 으로 접속하여 Key : companyId로 설정 후 value 셋팅
 
 - 예시

<img width="844" height="674" alt="image" src="https://github.com/user-attachments/assets/c00aab4f-3a66-4f02-a1bf-2ddd0b425c3d" />


# 4. AI(LLM 등)를 활용해서 위 과제를 진행한 경우, 아래에 해당사항이 있다면 작성해주세요.

이번 프로젝트를 통해 실제로 API 서비스를 데이터베이스 설계부터 개발, 배포까지 처음으로 직접 경험해보았습니다. 특히 Docker와 같은 기술은 처음 다뤄보는 것이었지만, AI 도구를 적극 활용하면서 부족한 부분을 빠르게 보완할 수 있었습니다.
그 과정에서, 내가 몰랐던 기술이라도 기존의 개발 경험과 학습 능력을 바탕으로 충분히 빠르게 익히고 적용할 수 있다는 자신감을 얻을 수 있었고, 시스템을 만드는 데 큰 도움이 되었습니다.


