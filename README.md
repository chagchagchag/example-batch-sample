
## 실행

실행 시 아래와 같이 해주세요.
- `java -jar {jar 이름}.jar --spring.batch.job.names={Job이름} --spring.profiles.active={원하는Profile 명}` 
<br/>

job 이름은 `job.name` 을 파라미터로 주는 예제 역시 많으며 그렇게 지정해도되는데, 만약 `job.name` 을 Program Argument 로 넘겨주실 것이라면, application-mysql-dev, application-h2.yml 에 아래와 같이 batch job 관련 프로퍼티를 수정해주세요.

```yaml
spring:
  batch:
    job:
      names: ${job.name:NONE}
```
<br/>

## 주의할 점
### public → private
Job 내에서 호출하는 Step, Reader, Writer 함수들은 가급적이면 private 으로 선언해주세요.<br/>
그래야 외부에서 호출되지 않습니다.<br/>
`@Bean` 으로 선언하지 않았음에도 애플리케이션 실행시 외부에서 실행되는 이슈가 있었습니다.<br/>
<br/>

이 부분에 대해서는 public 으로 선언해도 어떤 방식으로 해야 호출이 안되는지를 찾아보고 있습니다.<br/>
<br/>




