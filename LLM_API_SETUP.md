# 🚀 LLM API 키 설정 가이드

정치인 신뢰도 분석 시스템에서 사용하는 LLM API 키 설정 방법을 안내합니다.

## 📋 **필요한 API 키**

### 1. **OpenAI GPT API**

- **서비스**: ChatGPT, GPT-4 등
- **가격**: 토큰당 과금 (GPT-4: $0.03/1K input, $0.06/1K output)
- **설정 위치**: `docker-compose.yml` 또는 환경 변수
- **키 이름**: `OPENAI_API_KEY`

**설정 방법:**

```bash
# docker-compose.yml에서
environment:
  OPENAI_API_KEY: sk-your-openai-api-key-here
```

### 2. **Google Gemini API**

- **서비스**: Gemini Pro, Gemini Ultra
- **가격**: 토큰당 과금 (Gemini Pro: $0.0005/1K input, $0.0015/1K output)
- **설정 위치**: `docker-compose.yml` 또는 환경 변수
- **키 이름**: `GOOGLE_GEMINI_API_KEY`

**설정 방법:**

```bash
# docker-compose.yml에서
environment:
  GOOGLE_GEMINI_API_KEY: your-gemini-api-key-here
```

### 3. **Anthropic Claude API**

- **서비스**: Claude 3 Sonnet, Claude 3 Opus
- **가격**: 토큰당 과금 (Claude 3 Sonnet: $0.003/1K input, $0.015/1K output)
- **설정 위치**: `docker-compose.yml` 또는 환경 변수
- **키 이름**: `ANTHROPIC_CLAUDE_API_KEY`

**설정 방법:**

```bash
# docker-compose.yml에서
environment:
  ANTHROPIC_CLAUDE_API_KEY: sk-ant-your-claude-api-key-here
```

## 🔧 **설정 방법**

### **방법 1: docker-compose.yml 직접 수정 (개발용)**

```yaml
environment:
  OPENAI_API_KEY: sk-your-openai-api-key-here
  GOOGLE_GEMINI_API_KEY: your-gemini-api-key-here
  ANTHROPIC_CLAUDE_API_KEY: sk-ant-your-claude-api-key-here
```

### **방법 2: 환경 변수 파일 사용 (권장)**

```bash
# .env 파일 생성 (프로젝트 루트에)
OPENAI_API_KEY=sk-your-openai-api-key-here
GOOGLE_GEMINI_API_KEY=your-gemini-api-key-here
ANTHROPIC_CLAUDE_API_KEY=sk-ant-your-claude-api-key-here
```

### **방법 3: 시스템 환경 변수 설정**

```bash
# Windows
set OPENAI_API_KEY=sk-your-openai-api-key-here
set GOOGLE_GEMINI_API_KEY=your-gemini-api-key-here
set ANTHROPIC_CLAUDE_API_KEY=sk-ant-your-claude-api-key-here

# Linux/Mac
export OPENAI_API_KEY=sk-your-openai-api-key-here
export GOOGLE_GEMINI_API_KEY=your-gemini-api-key-here
export ANTHROPIC_CLAUDE_API_KEY=sk-ant-your-claude-api-key-here
```

## 🎯 **API 키 획득 방법**

### **OpenAI API 키**

1. [OpenAI Platform](https://platform.openai.com/) 접속
2. 계정 생성 및 로그인
3. API Keys 메뉴에서 새 키 생성
4. 생성된 키를 복사하여 설정

### **Google Gemini API 키**

1. [Google AI Studio](https://makersuite.google.com/app/apikey) 접속
2. Google 계정으로 로그인
3. API 키 생성 버튼 클릭
4. 생성된 키를 복사하여 설정

### **Anthropic Claude API 키**

1. [Anthropic Console](https://console.anthropic.com/) 접속
2. 계정 생성 및 로그인
3. API Keys 메뉴에서 새 키 생성
4. 생성된 키를 복사하여 설정

## ⚠️ **보안 주의사항**

### **개발 환경**

- API 키를 소스 코드에 직접 하드코딩하지 마세요
- `.env` 파일을 `.gitignore`에 추가하세요
- 공개 저장소에 API 키를 업로드하지 마세요

### **프로덕션 환경**

- 환경 변수나 시크릿 관리 시스템 사용
- API 키를 로그에 출력하지 마세요
- 정기적으로 API 키를 로테이션하세요

## 💰 **비용 최적화 팁**

### **토큰 사용량 줄이기**

1. **프롬프트 최적화**: 명확하고 간결한 프롬프트 작성
2. **배치 처리**: 여러 정치인을 한 번에 분석
3. **캐싱 활용**: 동일한 분석 결과 재사용

### **API 선택 가이드**

- **비용 우선**: Gemini API (가장 저렴)
- **품질 우선**: GPT-4 또는 Claude 3 Opus
- **균형**: GPT-3.5-turbo 또는 Claude 3 Sonnet

## 🚀 **실행 및 테스트**

### **1. API 키 설정 후 실행**

```bash
# Docker Compose로 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f factseeker-backend
```

### **2. API 테스트**

```bash
# 정치인 분석 실행
curl -X POST http://localhost:8080/api/politicians/analysis/execute

# 수동 분석 실행
curl -X POST http://localhost:8080/api/politicians/analysis/execute

# 결과 확인
curl http://localhost:8080/api/politicians/trust-scores
```

### **3. 로그 확인**

```bash
# LLM API 호출 로그 확인
docker-compose logs factseeker-backend | grep "GPT\|GEMINI\|CLAUDE"
```

## 🔍 **문제 해결**

### **API 키 인증 실패**

- API 키가 올바르게 설정되었는지 확인
- API 키에 충분한 크레딧이 있는지 확인
- API 서비스 상태 확인

### **토큰 한도 초과**

- API 사용량 및 한도 확인
- 비용 제한 설정 확인
- 필요시 더 높은 한도로 업그레이드

### **응답 시간 지연**

- 네트워크 연결 상태 확인
- API 서비스 상태 확인
- 필요시 다른 리전의 API 엔드포인트 사용

## 📚 **추가 리소스**

- [OpenAI API 문서](https://platform.openai.com/docs)
- [Google Gemini API 문서](https://ai.google.dev/docs)
- [Anthropic Claude API 문서](https://docs.anthropic.com/)
- [Spring Boot 환경 변수 설정](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
