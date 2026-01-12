package com.lien.repository;

import com.lien.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * ==============================================================================
 * User Repository (사용자 레포지토리)
 * ==============================================================================
 * 
 * 역할:
 * - User 엔티티의 데이터베이스 접근 계층
 * - JpaRepository를 상속받아 기본 CRUD 제공
 * - 사용자 인증 및 조회 기능 제공
 * 
 * 이점:
 * 1. Spring Data JPA의 메서드 자동 구현
 * 2. 타입 안전성 보장
 * 3. 보일러플레이트 코드 제거
 * 4. 쿼리 메서드 네이밍 규칙으로 자동 생성
 * 
 * JpaRepository 제공 메서드:
 * - save(User): 저장/수정
 * - findById(Long): ID로 조회
 * - findAll(): 전체 조회
 * - delete(User): 삭제
 * - count(): 개수 조회
 * - existsById(Long): 존재 여부 확인
 * 
 * 사용 예시:
 * <pre>
 * {@code
 * @Service
 * public class UserService {
 *     @Autowired
 *     private UserRepository userRepository;
 *     
 *     // 회원가입
 *     public User register(String email, String password, String name) {
 *         User user = User.builder()
 *             .email(email)
 *             .password(passwordEncoder.encode(password))
 *             .name(name)
 *             .enabled(true)
 *             .build();
 *         return userRepository.save(user);
 *     }
 *     
 *     // 로그인 (이메일 조회)
 *     public User login(String email) {
 *         return userRepository.findByEmail(email)
 *             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
 *     }
 *     
 *     // 계정 비활성화
 *     public void disableUser(Long userId) {
 *         User user = userRepository.findById(userId)
 *             .orElseThrow(() -> new EntityNotFoundException("User not found"));
 *         user.setEnabled(false);
 *         userRepository.save(user);
 *     }
 * }
 * }
 * </pre>
 * 
 * 실행되는 SQL 예시:
 * <pre>
 * -- findByEmail("user@example.com")
 * SELECT * FROM users WHERE email = 'user@example.com';
 * 
 * -- save(user)
 * INSERT INTO users (email, password, name, enabled, created_at, updated_at) 
 * VALUES ('user@example.com', '$2a$10$...', '홍길동', TRUE, NOW(), NOW());
 * 
 * -- findById(1L)
 * SELECT * FROM users WHERE id = 1;
 * 
 * -- findByEmailAndEnabled("user@example.com", true)
 * SELECT * FROM users WHERE email = 'user@example.com' AND enabled = TRUE;
 * </pre>
 * 
 * @see User
 * @see JpaRepository
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     * 
     * 역할:
     * - 로그인 시 사용자 인증
     * - 이메일 중복 체크
     * - 사용자 프로필 조회
     * 
     * 쿼리 생성:
     * - Spring Data JPA의 메서드 네이밍 규칙으로 자동 생성
     * - findBy + 필드명 = WHERE 절 생성
     * 
     * 실행 SQL:
     * <pre>
     * SELECT u FROM User u WHERE u.email = :email
     * </pre>
     * 
     * 성능:
     * - idx_user_email 인덱스 사용 (User 엔티티에 정의)
     * - O(1) 시간 복잡도 (인덱스 기반)
     * - Full Table Scan 방지
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 로그인 시
     * Optional<User> optionalUser = userRepository.findByEmail("user@example.com");
     * if (optionalUser.isPresent()) {
     *     User user = optionalUser.get();
     *     if (passwordEncoder.matches(rawPassword, user.getPassword())) {
     *         // 로그인 성공
     *         String token = jwtUtil.generateToken(user.getEmail());
     *         return token;
     *     }
     * }
     * 
     * // 이메일 중복 체크
     * if (userRepository.findByEmail(email).isPresent()) {
     *     throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
     * }
     * 
     * // Spring Security UserDetailsService 구현
     * @Override
     * public UserDetails loadUserByUsername(String email) {
     *     User user = userRepository.findByEmail(email)
     *         .orElseThrow(() -> new UsernameNotFoundException("User not found"));
     *     
     *     return org.springframework.security.core.userdetails.User
     *         .withUsername(user.getEmail())
     *         .password(user.getPassword())
     *         .disabled(!user.getEnabled())
     *         .build();
     * }
     * }
     * </pre>
     * 
     * @param email 조회할 이메일 (대소문자 구분)
     * @return Optional<User> 사용자 (존재하지 않으면 Optional.empty())
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일과 활성화 상태로 사용자 조회
     * 
     * 역할:
     * - 활성화된 계정만 로그인 허용
     * - 비활성화 계정 필터링
     * 
     * 실행 SQL:
     * <pre>
     * SELECT u FROM User u WHERE u.email = :email AND u.enabled = :enabled
     * </pre>
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 활성화된 사용자만 조회
     * Optional<User> user = userRepository.findByEmailAndEnabled("user@example.com", true);
     * if (!user.isPresent()) {
     *     throw new DisabledException("비활성화된 계정입니다");
     * }
     * }
     * </pre>
     * 
     * @param email 조회할 이메일
     * @param enabled 활성화 상태 (true: 활성, false: 비활성)
     * @return Optional<User> 조건에 맞는 사용자
     */
    Optional<User> findByEmailAndEnabled(String email, Boolean enabled);
    
    /**
     * 이메일 존재 여부 확인
     * 
     * 역할:
     * - 회원가입 시 이메일 중복 체크
     * - findByEmail보다 성능 우수 (COUNT 쿼리)
     * 
     * 실행 SQL:
     * <pre>
     * SELECT COUNT(u) FROM User u WHERE u.email = :email
     * </pre>
     * 
     * 성능 비교:
     * - findByEmail: 전체 컬럼 조회 후 존재 여부 확인
     * - existsByEmail: COUNT만 조회 (더 빠름)
     * 
     * 사용 예시:
     * <pre>
     * {@code
     * // 회원가입 시 중복 체크
     * if (userRepository.existsByEmail(email)) {
     *     throw new DuplicateEmailException("이미 사용 중인 이메일입니다");
     * }
     * 
     * User newUser = userRepository.save(user);
     * }
     * </pre>
     * 
     * @param email 확인할 이메일
     * @return boolean true: 존재, false: 미존재
     */
    boolean existsByEmail(String email);
} 