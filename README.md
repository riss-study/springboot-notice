# Notice Management Service

## 개요
이 프로젝트는 공지사항 관리 REST API입니다. 주요 기능으로는 공지사항의 등록, 수정, 삭제, 조회, 첨부파일 업로드 및 삭제가 있습니다. 이 API는 Spring Boot를 기반으로 하며, 비즈니스 로직은 서비스와 레포지토리를 통해 관리됩니다.

## 주요 기능
1. **공지사항 등록**: 사용자가 입력한 공지사항 데이터를 데이터베이스에 저장합니다.
2. **공지사항 조회**: 공지사항 목록을 페이징 처리하여 조회하며, 특정 공지사항의 상세 정보도 조회할 수 있습니다.
3. **공지사항 수정**: 기존에 등록된 공지사항을 수정합니다.
4. **공지사항 삭제**: 공지사항과 연결된 첨부파일을 포함하여 삭제합니다.
5. **첨부파일 업로드**: 공지사항에 첨부파일을 업로드하고, 이를 공지사항과 연결합니다.
6. **첨부파일 삭제**: 업로드된 첨부파일을 삭제합니다.
7. **조회수 카운팅**: 공지사항 조회 시 조회수를 비동기적으로 증가시키고, 주기적으로 DB에 동기화합니다.

## 실행 방법

### 요구 사항
- JDK 17 이상
- Spring Boot
- MySQL 8.0.x
- Lombok
- Gradle

### 1. 환경 설정

- `application.yml`에서 설정을 수정해야 합니다.

```properties
# 서버 주소
notice.domain=http://localhost:8080

# 첨부파일 경로 (변경 가능)
notice.attachment.directory=uploads
```

### 2. 빌드 및 실행

1. 빌드
```shell
./gradlew build
```

2. 실행
```shell
java -jar build/libs/notice-0.0.1-SNAPSHOT.jar
```

3. 테스트 실행
```shell
./gradlew test
```

### 3. API 호출 예시

#### 1. 공지사항 등록
**POST /api/v1/notice**
- 게시글을 새로 등록합니다.
- **Request Body**:
  ```json
  {
    "title": "게시글 제목",
    "content": "게시글 내용",
    "startAt": "2025-01-01 00:00:00",
    "endAt": "2025-01-10 23:59:59",
    "author": "작성자"
  }
  ```
  
- **Response**:
  ```json
  {
    "status": "success",
    "data": {
    "uid": 1
    }
  }
  ```

#### 2. 게시글 수정 API
**PUT /api/v1/notice/{uid}**
- 게시글을 수정합니다.
- Path Parameter: uid (게시글 UID)
- **Request Body**:
  ```json
  {
    "title": "수정된 제목",
    "content": "수정된 내용",
    "startAt": "2025-01-01 00:00:00",
    "endAt": "2025-01-10 23:59:59",
    "author": "수정된 작성자"
  }
  ```

- **Response**:
  ```json
  {
    "status": "success",
    "data": null
  }
  ```

#### 3. 게시글 삭제 API
**DELETE /api/v1/notice/{uid}**
- 게시글을 삭제합니다.
- Path Parameter: uid (게시글 UID)
- **Response**:
  ```json
  {
    "status": "success",
    "data": null
  }
  ```

#### 4. 게시글 전체 조회 API
**GET /api/v1/notice/all**
- 전체 게시글을 조회합니다. 페이지 번호는 쿼리 파라미터로 전달합니다.
- Query Parameter: page (기본값: 0)
- **Response**:
  ```json
  {
    "status": "success",
    "data": [
      {
        "uid": 1,
        "title": "게시글 제목",
        "content": "게시글 내용",
        "startAt": "2025-01-01 00:00:00",
        "endAt": "2025-01-10 23:59:59",
        "author": "작성자",
        "views": 100
      }
    ]
  }
  ```

#### 5. 게시글 단건 조회 API
**GET /api/v1/notice/{uid}**
- 게시글을 조회합니다.
- Path Parameter: uid (게시글 UID)
- **Response**:
  ```json
  {
    "success": true,
    "message": "",
    "data": {
      "uid": 1,
      "title": "게시글 제목",
      "content": "게시글 내용",
      "createdAt": "2025-01-28 01:29:43",
      "views": 100,
      "author": "작성자",
      "noticeAttachmentDtoList": [
        {
          "uid": 1,
          "originFileName": "파일1.hwp",
          "fileUrl": "http://localhost:8080/uploads/1_12c1a9e3-6deb-4067-8e43-046614549945.hwp"
        },
        {
          "uid": 2,
          "originFileName": "파일2.png",
          "fileUrl": "http://localhost:8080/uploads/1_820892d2-f0a8-414b-8e69-da0066c44873.png"
        }
      ]
    }
  }
  ```

#### 6. 게시글 첨부파일 등록 API
**POST /api/v1/notice/{uid}/attachment**
- 게시글 첨부파일을 등록합니다.
- Path Parameter: uid (게시글 UID)
- Request: multipart/form-data 로 첨부파일(key: attachments)을 전달합니다.
- **Response**:
  ```json
  {
    "status": "success",
    "data": null
  }
  ```

#### 7. 게시글 첨부파일 삭제 API
**POST /api/v1/notice/{uid}/attachment/bulk-delete**
- 게시글의 첨부파일을 삭제합니다.
- Path Parameter: uid (게시글 UID)
- **Request Body**:
  ```json
  {
    "attachmentIdList": [1, 2, 3]
  }

  ```
- **Response**:
  ```json
  {
    "status": "success",
    "data": null
  }
  ```
  
### 4. Entity 구조 및 관계
1. 구조
```text
+---------------------------+         +---------------------------+       +-----------------------------+
|          Notice           |         |         Attachment        |       |          BaseEntity         |
+---------------------------+         +---------------------------+       +-----------------------------+
| - uid (Long)              |         | - uid (Long)              |       | - createdAt (LocalDateTime) |
| - title (String)          |         | - originFileName (String) |       | - updatedAt (LocalDateTime) |
| - content (String)        |         | - newFileName (String)    |       +-----------------------------+
| - startAt (LocalDateTime) |         | - path (String)           |       
| - endAt (LocalDateTime)   |         | - notice (Notice)         |
| - views (Long)            |         +---------------------------+
| - author (String)         |
+---------------------------+
```
2. 관계
- Notice는 여러 개의 Attachment를 가질 수 있습니다. (OneToMany)
- Attachment는 하나의 Notice에 속합니다. (ManyToOne)
- BaseEntity는 createdAt, updatedAt 필드를 포함하여 모든 엔티티가 이 클래스를 상속받습니다.


### 5. 핵심 문제 해결 전략
#### 1. 대용량 데이터 처리
- 공지사항 조회 및 첨부파일 관리와 같은 대용량 데이터를 효율적으로 처리하기 위해 여러 전략을 사용했습니다.
1. 페이징 처리
- findAll() 메소드에서 공지사항 목록을 조회할 때 Pageable 객체를 사용하여 한 번에 모든 데이터를 불러오지 않고, 클레이언트에게 필요한 데이터만 전송하여 효율적인 메모리 관리가 가능합니다.
```java
PageRequest pageRequest = PageRequest.of(pageNo, PAGE_SIZE);
Page<Notice> page = noticeRepository.findAllByEndAtAfter(LocalDateTime.now(), pageRequest);
```

2. 조회수 비동기 처리 및 캐싱
- 조회수가 증가하는 방식을 비동기로 처리했습니다. 공지사항 조회 시 조회수 증가를 즉시 반영하는 대신, viewCountCache에 조회수를 임시로 저장하고, 주기적으로(@Scheduled 어노테이션 이용) 이를 데이터베이스에 동기화하도록 했습니다.
- 이를 통해, 조회수가 증가할 때마다 DB에 직접적으로 접근하는 것을 방지하고, 서버 부하를 줄여 성능을 개선했습니다.
- 서버 개수가 늘어나면, viewCountCache를 대신 Redis 혹은 메시지큐잉을 이용하여 메모리서버에 캐싱하는 것으로 대체할 수 있습니다.
```java
@Scheduled(fixedRate = 60000)
public void syncViewCountsToDatabase() {
    for (Map.Entry<Long, Long> entry : viewCountCache.entrySet()) {
        noticeRepository.incrementViewCount(entry.getKey(), entry.getValue());
    }
    viewCountCache.clear();
}
```

3. 첨부파일 관리
- 첨부파일을 저장하기 전, 파일 이름을 UUID로 변경하여 충돌을 방지했습니다
- 첨부파일 업로드는 다중 파일을 동시에 처리할 수 있도록 구현했습니다. @Async 어노테이션을 사용하여 비동기적으로 처리하였습니다.
- 서버의 주요 흐름에 영향을 주지 않으며, 대용량 파일 처리 시에도 서버 부하를 최소화할 수 있습니다.
```java
@Async
protected CompletableFuture<Void> storeFile(MultipartFile file, String newFileName) {
    return CompletableFuture.runAsync(() -> {
        Path targetLocation = Paths.get(NOTICE_ATTACHMENT_PATH + "/" + newFileName).toAbsolutePath().normalize();
        File dir = new File(NOTICE_ATTACHMENT_PATH);
        if (!dir.exists()) dir.mkdirs();

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException("첨부파일 업로드에 실패했습니다. 파일 제목: " + file.getOriginalFilename());
        }

    }).exceptionally(ex -> {
        throw new ApiException("첨부파일 업로드에 실패했습니다. 파일 제목: " + file.getOriginalFilename());
    });
}
```
- 첨부파일 삭제도 비동기로 구현했으며, 삭제 요청 시 파일 시스템에서 파일을 삭제하고, DB에서 해당 파일 정보를 제거하도록 구현했습니다.
```java
@Async
protected CompletableFuture<Void> deleteFile(Attachment findAttachment) {
    return CompletableFuture.runAsync(() -> {
        Path path = Paths.get(NOTICE_ATTACHMENT_PATH + "/" + findAttachment.getNewFileName());
        try {
            if (!Files.exists(path)) return;
            Files.delete(path);
        } catch (IOException e) {
            throw new ApiException("해당 파일 삭제에 실패했습니다. 파일이름: " + findAttachment.getNewFileName());
        }
    });
}
```
#### 2. 예외처리
- ApiException: API 호출 중 발생한 예외는 ApiException으로 처리됩니다.
- 각 메서드에서는 예외 발생 시 적절한 메시지를 포함하여 예외를 던집니다.
- @Async 어노테이션을 사용하는 비동기처리 시 호출자에게 직접적인 예외를 던지지 않기 때문에, Future 인터페이스의 구현체인 CompletableFuture 객체를 리턴하여 호출자가 직접 get() 메서드를 통해 예외를 던질 수 있도록 구현했습니다.