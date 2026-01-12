package com.lien.service;

import com.lien.entity.User;
import com.lien.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ==============================================================================
 * User Service (사용자 서비스)
 * ==============================================================================
 * 
 * 역할:
 * - 사용자 인증 및 회원가입 비즈니스 로직
 * - 비밀번호 암호화 및 검증
 * - 이메일 중복 체크
 * 
 * 이점:
 * 1. BCrypt 암호화로 보안 강화
 * 2. 트랜잭션 관리로 데이터 일관성 보장
 * 3. 비즈니스 로직과 데이터 접근 계층 분리
 * 4. 재사용 가능한 인증 로직
 * 
 * 보안:
 * - BCrypt: Salt 자동 생성 (Rainbow Table 공격 방어)
 * - 동일한 비밀번호도 매번 다른 해시 생성
 * - 원본 비밀번호는 절대 저장하지 않음
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @RestController
 * public class AuthController {
 *     @Autowired
 *     private UserService userService;
 *     
 *     @Autowired
 *     private JwtUtil jwtUtil;
 *     
 *     // 회원가입
 *     @PostMapping("/api/auth/register")
 *     public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
 *         User user = userService.register(
 *             request.getEmail(),
 *             request.getPassword(),
 *             request.getName()
 *         );
 *         return ResponseEntity.ok(user);
 *     }
 *     
 *     // 로그인
 *     @PostMapping("/api/auth/login")
 *     public ResponseEntity<?> login(@RequestBody LoginRequest request) {
 *         User user = userService.authenticate(
 *             request.getEmail(),
 *             request.getPassword()
 *         );
 *         
 *         String token = jwtUtil.generateToken(user.getEmail());
 *         return ResponseEntity.ok(new LoginResponse(token, user));
 *     }
 * }
 * }
 * </pre>
 * 
 * @see User
 * @see UserRepository
 */
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 생성자 주입
     * 
     * 이점:
     * - 불변성 보장 (final 필드)
     * - 테스트 용이성 (Mock 주입 가능)
     * - 순환 참조 방지
     * 
     * @param userRepository 사용자 레포지토리
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 회원가입
     * 
     * 역할:
     * - 새로운 사용자 등록
     * - 이메일 중복 체크
     * - 비밀번호 암호화
     * 
     * 처리 흐름:
     * 1. 이메일 중복 확인
     * 2. 비밀번호 BCrypt 암호화
     * 3. User 엔티티 생성
     * 4. 데이터베이스 저장
     * 
     * 트랜잭션:
     * - @Transactional: 전체 작업이 하나의 트랜잭션으로 실행
     * - 중복 체크와 저장 사이의 동시성 문제 방지
     * - 오류 발생 시 자동 롤백
     * 
     * BCrypt 암호화 예시:
     * <pre>
     * 원본: "password123"
     * 암호화: "$2a$10$N9qo8uLOickgx2ZMRZoMye1Yk8MqQ5Pz5KQNg.M8QqYxYzWz.1k6W"
     * 
     * 특징:
     * - $2a$: BCrypt 알고리즘 버전
     * - 10: Cost Factor (반복 횟수 2^10 = 1024)
     * - 나머지: Salt + Hash
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 정상 케이스
     * User user = userService.register("user@example.com", "password123", "홍길동");
     * System.out.println("회원가입 성공: " + user.getName());
     * 
     * // 중복 이메일 에러
     * try {
     *     userService.register("user@example.com", "password456", "김철수");
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); // "이미 가입된 이메일입니다."
     * }
     * }
     * </pre>
     * 
     * @param email 사용자 이메일 (고유 식별자)
     * @param password 원본 비밀번호 (암호화되어 저장됨)
     * @param name 사용자 이름
     * @return User 등록된 사용자 (비밀번호는 암호화됨)
     * @throws IllegalArgumentException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public User register(String email, String password, String name) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        
        // 비밀번호 암호화
        // BCrypt는 Salt를 자동 생성하여 동일 비밀번호도 다른 해시 생성
        String encodedPw = passwordEncoder.encode(password);
        
        // User 엔티티 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(encodedPw)
                .name(name)
                .enabled(true)  // 기본적으로 활성화
                .build();
        
        return userRepository.save(user);
    }

    /**
     * 사용자 인증 (로그인)
     * 
     * 역할:
     * - 이메일과 비밀번호로 사용자 인증
     * - 비밀번호 일치 여부 확인
     * 
     * 처리 흐름:
     * 1. 이메일로 사용자 조회
     * 2. 사용자 존재 여부 확인
     * 3. 비밀번호 일치 여부 확인 (BCrypt)
     * 4. 인증 성공 시 User 반환
     * 
     * BCrypt 검증:
     * - passwordEncoder.matches(원본, 암호화된 비밀번호)
     * - Salt를 자동으로 추출하여 비교
     * - 타이밍 공격 방어 (항상 일정 시간 소요)
     * 
     * 보안 고려사항:
     * - 이메일과 비밀번호 오류를 구분하지 않음 (정보 노출 방지)
     * - "이메일이 존재하지 않습니다" (X)
     * - "이메일 또는 비밀번호가 올바르지 않습니다" (O)
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 정상 케이스
     * User user = userService.authenticate("user@example.com", "password123");
     * System.out.println("로그인 성공: " + user.getName());
     * 
     * // JWT 토큰 생성
     * String token = jwtUtil.generateToken(user.getEmail());
     * 
     * // 실패 케이스 1: 존재하지 않는 이메일
     * try {
     *     userService.authenticate("unknown@example.com", "password123");
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); 
     *     // "이메일 또는 비밀번호가 올바르지 않습니다."
     * }
     * 
     * // 실패 케이스 2: 비밀번호 불일치
     * try {
     *     userService.authenticate("user@example.com", "wrongpassword");
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); 
     *     // "이메일 또는 비밀번호가 올바르지 않습니다."
     * }
     * }
     * </pre>
     * 
     * 추가 기능 제안:
     * <pre>
     * {@code
     * // 계정 활성화 상태 확인
     * public User authenticate(String email, String password) {
     *     User user = userRepository.findByEmailAndEnabled(email, true)
     *         .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
     *     
     *     if (!passwordEncoder.matches(password, user.getPassword())) {
     *         throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
     *     }
     *     
     *     return user;
     * }
     * 
     * // 로그인 실패 횟수 제한
     * private static final int MAX_LOGIN_ATTEMPTS = 5;
     * 
     * public User authenticate(String email, String password) {
     *     // Redis에서 실패 횟수 확인
     *     int attempts = getLoginAttempts(email);
     *     if (attempts >= MAX_LOGIN_ATTEMPTS) {
     *         throw new AccountLockedException("계정이 잠겼습니다. 관리자에게 문의하세요.");
     *     }
     *     
     *     try {
     *         User user = ...
     *         // 성공 시 실패 횟수 초기화
     *         resetLoginAttempts(email);
     *         return user;
     *     } catch (Exception e) {
     *         // 실패 시 횟수 증가
     *         incrementLoginAttempts(email);
     *         throw e;
     *     }
     * }
     * }
     * </pre>
     * 
     * @param email 사용자 이메일
     * @param password 원본 비밀번호
     * @return User 인증된 사용자
     * @throws IllegalArgumentException 이메일 또는 비밀번호가 올바르지 않은 경우
     */
    public User authenticate(String email, String password) {
        // 이메일로 사용자 조회
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // 사용자 존재 여부 및 비밀번호 일치 확인
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            // 보안: 이메일 존재 여부를 노출하지 않음
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        return userOpt.get();
    }
    
    /**
     * 이메일 중복 체크
     * 
     * 역할:
     * - 회원가입 전 이메일 중복 확인
     * - 실시간 중복 체크 API 제공
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // Controller에서 사용
     * @GetMapping("/api/auth/check-email")
     * public ResponseEntity<?> checkEmail(@RequestParam String email) {
     *     boolean exists = userService.isEmailExists(email);
     *     return ResponseEntity.ok(Map.of("exists", exists));
     * }
     * }
     * </pre>
     * 
     * @param email 확인할 이메일
     * @return boolean true: 존재, false: 미존재
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 사용자 ID로 조회
     * 
     * 역할:
     * - 사용자 ID로 정보 조회
     * - JWT 토큰의 사용자 정보 검증
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // JWT에서 이메일 추출 후 사용자 조회
     * String email = jwtUtil.extractEmail(token);
     * User user = userService.findByEmail(email);
     * }
     * </pre>
     * 
     * @param email 이메일
     * @return Optional<User> 사용자 (없으면 Optional.empty())
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
} 