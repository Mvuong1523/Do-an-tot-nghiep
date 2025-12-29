# NGÔN NGỮ VÀ FRAMEWORK SỬ DỤNG XÂY DỰNG FRONTEND

## Lời mở đầu

Trong bối cảnh cuộc cách mạng công nghiệp 4.0 đang diễn ra mạnh mẽ, công nghệ thông tin đã và đang trở thành động lực chính thúc đẩy sự phát triển kinh tế - xã hội trên toàn cầu nói chung và Việt Nam nói riêng. Thương mại điện tử (E-commerce) không còn là khái niệm xa lạ mà đã trở thành xu hướng tất yếu, thay đổi căn bản cách thức mua bán và tiêu dùng của người dân. Theo báo cáo của Hiệp hội Thương mại điện tử Việt Nam, quy mô thị trường thương mại điện tử Việt Nam đạt hàng tỷ USD và tiếp tục tăng trưởng mạnh mẽ qua từng năm.

Đặc biệt trong lĩnh vực kinh doanh sản phẩm công nghệ, nhu cầu mua sắm trực tuyến ngày càng gia tăng do tính tiện lợi, đa dạng sản phẩm và khả năng so sánh giá cả dễ dàng. Người tiêu dùng hiện đại không còn bị giới hạn bởi không gian và thời gian - họ có thể tìm kiếm, so sánh và đặt mua sản phẩm công nghệ chỉ với vài thao tác đơn giản trên thiết bị di động hoặc máy tính có kết nối internet. Điều này không chỉ tiết kiệm thời gian mà còn mang lại trải nghiệm mua sắm tốt hơn với đầy đủ thông tin chi tiết về sản phẩm, đánh giá từ người dùng khác, và các chương trình khuyến mãi hấp dẫn.

Tuy nhiên, để xây dựng một website thương mại điện tử thành công, đặc biệt là trong lĩnh vực công nghệ với hàng nghìn sản phẩm đa dạng, đòi hỏi không chỉ giao diện đẹp mắt mà còn cần có hệ thống quản lý toàn diện. Từ quản lý kho hàng, xử lý đơn hàng, tích hợp vận chuyển, đến quản lý tài chính kế toán - tất cả cần được tự động hóa và đồng bộ để đảm bảo hiệu quả vận hành. Đồng thời, hệ thống cần phân quyền rõ ràng cho các vai trò khác nhau như quản trị viên, nhân viên kho, nhân viên kế toán, nhân viên bán hàng để đảm bảo tính bảo mật và hiệu quả công việc.

Nhận thức được những nhu cầu thực tế đó cũng như mong muốn ứng dụng kiến thức đã học vào thực tiễn, em đã thực hiện đề tài: "Xây dựng website bán đồ công nghệ". Mục tiêu của đề tài là xây dựng một hệ thống thương mại điện tử hoàn chỉnh, không chỉ phục vụ khách hàng mua sắm trực tuyến mà còn hỗ trợ toàn diện cho việc quản lý vận hành doanh nghiệp. Hệ thống được thiết kế với giao diện thân thiện, dễ sử dụng, tích hợp đầy đủ các tính năng từ quản lý sản phẩm, xử lý đơn hàng, quản lý kho vận, tích hợp vận chuyển với Giao Hàng Nhanh (GHN), đến module kế toán tự động tính thuế và báo cáo tài chính. Đặc biệt, hệ thống được xây dựng trên nền tảng công nghệ hiện đại với kiến trúc phân tầng rõ ràng, đảm bảo khả năng mở rộng và bảo trì trong tương lai.

---

## Giới thiệu về HTML

HTML (HyperText Markup Language) là ngôn ngữ đánh dấu được sử dụng để tạo và cấu trúc nội dung trên World Wide Web. HTML cung cấp các thành phần cần thiết để định dạng văn bản, hình ảnh, liên kết, và các yếu tố khác trên trang web.

### Vai trò của HTML trong dự án

Trong dự án này, HTML được sử dụng thông qua JSX (JavaScript XML) - một cú pháp mở rộng của JavaScript cho phép viết code giống HTML trong các file TypeScript/JavaScript. Mọi giao diện người dùng đều được xây dựng dựa trên cấu trúc HTML cơ bản.

### Ưu điểm của HTML

- **Được sử dụng rộng rãi**: HTML là nền tảng của mọi trang web, có nhiều tài nguyên hỗ trợ và cộng đồng lớn
- **Cú pháp dễ hiểu**: HTML có cú pháp đơn giản, dễ học, phù hợp với cả người mới bắt đầu
- **Tương thích đa nền tảng**: HTML được hỗ trợ bởi tất cả các trình duyệt web, đảm bảo nội dung hiển thị đúng trên nhiều thiết bị khác nhau
- **Tích hợp tốt**: HTML dễ dàng tích hợp với CSS và JavaScript để tạo ra các trang web phong phú và tương tác

### Ứng dụng trong dự án

Trong dự án, HTML được sử dụng để xây dựng cấu trúc của mọi trang web, từ trang tổng quan kế toán đến trang quản lý sản phẩm. Các thẻ HTML như div, h1, p được kết hợp với các thuộc tính className để tạo nên bố cục và cấu trúc rõ ràng cho giao diện người dùng. Ví dụ, trang kế toán sử dụng các thẻ div để tạo container, thẻ h1 cho tiêu đề chính, và thẻ p cho các đoạn văn bản mô tả.

## Giới thiệu về CSS

CSS (Cascading Style Sheets) là ngôn ngữ dùng để mô tả cách hiển thị của các phần tử HTML trên trang web. CSS kiểm soát giao diện của trang web bằng cách quy định các kiểu dáng (style) như màu sắc, font chữ, bố cục, và nhiều thuộc tính khác.

### Tính năng và lợi ích của CSS

**Tách biệt nội dung và giao diện**: CSS giúp tách biệt phần nội dung HTML và phần giao diện, làm cho mã nguồn dễ quản lý và bảo trì hơn.

**Tái sử dụng**: Một tệp CSS có thể được sử dụng lại cho nhiều trang web, giúp tiết kiệm thời gian và công sức.

**Kiểm soát toàn bộ trang web**: CSS cho phép thay đổi giao diện của toàn bộ trang web từ một tệp duy nhất, thay vì phải thay đổi từng phần tử HTML.

**Tính tương thích**: CSS được hỗ trợ bởi tất cả các trình duyệt web hiện đại, đảm bảo rằng trang web sẽ hiển thị đúng trên nhiều thiết bị và nền tảng khác nhau.

**Tăng tốc độ tải trang**: Sử dụng CSS có thể giúp tăng tốc độ tải trang web bằng cách giảm kích thước tập tin và giảm thời gian tải của trang.

### CSS trong dự án - Tailwind CSS

Dự án sử dụng Tailwind CSS phiên bản 3.3.5, một utility-first CSS framework hiện đại. Thay vì viết CSS truyền thống trong các file riêng biệt, Tailwind cung cấp các class có sẵn để styling nhanh chóng. Ví dụ, để tạo một card hiển thị thông tin doanh thu, chỉ cần sử dụng các class như bg-white để tạo nền trắng, rounded-lg để bo góc, shadow để tạo bóng đổ, và p-6 để thêm padding. Các class text-sm, text-gray-600 được dùng để định dạng văn bản, trong khi text-2xl, font-bold, text-green-600 được sử dụng cho số liệu doanh thu nổi bật.

**Ưu điểm của Tailwind CSS**:
- Styling nhanh với utility classes có sẵn
- Responsive design dễ dàng với các breakpoints sm, md, lg, xl
- Customization linh hoạt với theme configuration
- Performance tốt với PurgeCSS tự động loại bỏ CSS không dùng

## Giới thiệu về JavaScript

JavaScript là một ngôn ngữ lập trình thông dịch với khả năng hướng đối tượng, được sử dụng rộng rãi trong việc phát triển các ứng dụng web. JavaScript cho phép các nhà phát triển tạo ra các trang web động và tương tác, nâng cao trải nghiệm người dùng trên các trang web.

### Các tính năng của JavaScript

**Thao tác với DOM**: JavaScript có thể truy cập và thay đổi các phần tử HTML và CSS trên trang web thông qua Document Object Model (DOM).

**Event handling**: JavaScript có thể xử lý các sự kiện người dùng như nhấp chuột, nhập liệu, di chuyển chuột, và nhiều sự kiện khác.

**AJAX**: JavaScript cho phép gửi và nhận dữ liệu từ máy chủ mà không cần tải lại trang, giúp tạo ra các ứng dụng web động và mượt mà hơn.

**Tạo hiệu ứng hình ảnh và động**: Tạo ra các hiệu ứng động trên trang web, chẳng hạn như chuyển động, thay đổi màu sắc, hoặc ẩn/hiện các phần tử.

### JavaScript trong dự án

Dự án sử dụng JavaScript thông qua TypeScript và React để xây dựng các tính năng tương tác. Ví dụ, khi người dùng gửi form để tạo giao dịch kế toán, JavaScript sẽ xử lý sự kiện submit, hiển thị trạng thái loading, gọi API để lấy dữ liệu từ server, xử lý response trả về, và cuối cùng cập nhật giao diện với dữ liệu mới. Trong quá trình này, JavaScript sử dụng useState để quản lý trạng thái loading, async/await để xử lý bất đồng bộ, và try-catch để xử lý lỗi một cách an toàn.

## Giới thiệu về TypeScript

TypeScript là một ngôn ngữ lập trình hướng đối tượng, mã nguồn mở, được Microsoft phát triển và duy trì. TypeScript là một strict superset của JavaScript, cho phép sử dụng toàn bộ cú pháp JavaScript đồng thời bổ sung hệ thống kiểu dữ liệu tĩnh, giúp phát hiện lỗi ngay trong quá trình biên dịch.

Mã TypeScript được biên dịch sang JavaScript nên vẫn đảm bảo khả năng tương thích với các trình duyệt và hệ sinh thái JavaScript hiện có. Việc sử dụng TypeScript trong đồ án giúp tăng tính an toàn của mã nguồn, nâng cao khả năng bảo trì và phù hợp với các ứng dụng có quy mô lớn, nhiều thành phần và logic phức tạp.

### Các tính năng chính của TypeScript

**Kiểm tra kiểu tĩnh**: TypeScript cho phép kiểm tra và gán kiểu cho các biến, tham số và giá trị trả về của hàm. Trong dự án, mỗi đối tượng User được định nghĩa rõ ràng với các thuộc tính như id kiểu string, email kiểu string, fullName kiểu string, role có thể là CUSTOMER, ADMIN hoặc EMPLOYEE, và position tùy chọn có thể là WAREHOUSE, ACCOUNTANT hoặc SALE. Điều này giúp phát hiện lỗi ngay khi viết code, ví dụ nếu gán role là một giá trị không hợp lệ, TypeScript sẽ báo lỗi ngay lập tức.

**Đối tượng dựa trên lớp**: TypeScript hỗ trợ đầy đủ lập trình hướng đối tượng với classes, interfaces, và access modifiers như public, private, protected. Điều này giúp tổ chức code tốt hơn và dễ bảo trì.

**Tính module**: Bằng cách sử dụng mô-đun, có thể sắp xếp mã thành các phần nhỏ hơn, có thể tái sử dụng. Tính mô-đun này tăng cường khả năng bảo trì và cộng tác giữa các thành viên trong nhóm.

**Tính năng của ES6+**: TypeScript bao gồm các tính năng của ECMAScript 6 và các phiên bản mới hơn như arrow functions, destructuring, async/await, modules.

### Cấu hình TypeScript trong dự án

Dự án sử dụng TypeScript phiên bản 5.2.2 với cấu hình target là es2020, hỗ trợ DOM và các tính năng ES2020, module system là esnext, và jsx được preserve để Next.js xử lý. Chế độ strict được tắt để linh hoạt hơn trong quá trình phát triển, và esModuleInterop được bật để tương thích tốt với các module CommonJS.

## Giới thiệu về thư viện React.js

React.js (thường được gọi là React) là một thư viện JavaScript mã nguồn mở được phát triển bởi Facebook, được sử dụng để xây dựng giao diện người dùng (UI) cho các ứng dụng web. React nổi bật với khả năng tạo ra các ứng dụng web một cách nhanh chóng và hiệu quả, nhờ vào một số tính năng mạnh mẽ và triết lý phát triển đặc trưng.

### Các đặc điểm chính của React.js

**Component-based architecture**: React xây dựng UI bằng cách chia nhỏ giao diện thành các thành phần (components) có thể tái sử dụng. Mỗi component là một phần tử UI độc lập, có thể được kết hợp lại để tạo nên giao diện phức tạp hơn.

**Virtual DOM**: React sử dụng một phiên bản DOM ảo (Virtual DOM) để tối ưu hóa việc cập nhật giao diện. Khi trạng thái của một component thay đổi, React tạo ra một DOM ảo mới và so sánh với DOM ảo cũ. Sau đó, nó chỉ cập nhật những phần của DOM thật sự cần thay đổi, giúp cải thiện hiệu suất đáng kể.

**One-way data binding**: Dữ liệu trong React được truyền từ cha xuống con thông qua props, điều này giúp kiểm soát dòng chảy dữ liệu một cách rõ ràng và dễ dàng theo dõi.

**JSX (JavaScript XML)**: JSX là một cú pháp mở rộng cho JavaScript, cho phép viết code giống như HTML trong JavaScript. JSX giúp dễ dàng tạo và cấu trúc các component trong React.

**State management**: Mỗi component trong React có thể có trạng thái riêng (state), giúp quản lý và theo dõi sự thay đổi của dữ liệu động trong ứng dụng.

### React.js trong dự án

**Phiên bản**: React 18.2.0

Dự án sử dụng React để xây dựng toàn bộ giao diện người dùng với các component có thể tái sử dụng. Ví dụ, component AccountingSummary nhận vào một prop là period (kỳ kế toán) và hiển thị tổng quan tài chính cho kỳ đó. Component này sử dụng useState để quản lý dữ liệu và trạng thái loading, useEffect để tự động fetch dữ liệu khi period thay đổi, và hiển thị loading spinner trong khi đang tải dữ liệu. Khi dữ liệu đã sẵn sàng, component sẽ render một card với tiêu đề và nội dung tương ứng.

**Các tính năng React được sử dụng trong dự án**:
- **Hooks**: useState để quản lý state, useEffect để xử lý side effects, useCallback để memoize functions, useMemo để memoize values, useRef để tham chiếu DOM elements
- **Custom Hooks**: useAuth để quản lý authentication, usePermission để kiểm tra quyền, useTranslation để đa ngôn ngữ
- **Component Composition**: Kết hợp nhiều components nhỏ như Header, Sidebar, Footer thành UI phức tạp
- **Conditional Rendering**: Hiển thị UI khác nhau dựa trên điều kiện như role của user, trạng thái loading
- **Event Handling**: Xử lý các sự kiện người dùng như click button, submit form, change input

## Giới thiệu về framework Next.js

Next.js là một framework dựa trên React được thiết kế để nâng cao quá trình phát triển các ứng dụng web bằng cách cung cấp nhiều tính năng tích hợp như server-side rendering (SSR), static site generation (SSG) và API routes. Nó nhằm mục đích đơn giản hóa các khía cạnh phức tạp của phát triển web, cho phép các nhà phát triển tập trung vào việc xây dựng các ứng dụng của họ thay vì phải xử lý cấu hình và thiết lập. Next.js nhanh và hoạt động tốt với các công cụ và thư viện khác.

### Các tính năng chính của Next.js

**Server-Side Rendering (SSR)**: Next.js cho phép các nhà phát triển dựng trước các trang trên máy chủ theo thời gian yêu cầu, mang lại hiệu suất và SEO tốt hơn so với dựng trước phía máy khách truyền thống.

**Static Site Generation (SSG)**: Next.js cung cấp chức năng tạo trang tĩnh cho các trang không thay đổi thường xuyên. Quy trình này bao gồm việc tạo các trang HTML tại thời điểm dựng, sau đó được phục vụ trực tiếp cho người dùng từ CDN.

**API Routes**: Next.js cung cấp một cách đơn giản để tạo các điểm cuối API trong ứng dụng, loại bỏ nhu cầu về một máy chủ phụ trợ riêng biệt.

**File-Based Routing**: Next.js sử dụng hệ thống định tuyến dựa trên tệp, trong đó các tuyến được tạo bằng cách chỉ cần thêm tệp và thư mục vào thư mục app.

**Tự động chia mã**: Next.js tự động chia mã thành các phần nhỏ hơn, đảm bảo chỉ tải JavaScript cần thiết cho mỗi trang.

**Hot Module Replacement (HMR)**: HMR cho phép các nhà phát triển xem các thay đổi theo thời gian thực mà không cần phải làm mới trình duyệt, giúp tăng tốc đáng kể quá trình phát triển.

**Hỗ trợ CSS và SASS tích hợp**: Next.js bao gồm hỗ trợ nhập trực tiếp các tệp CSS và SASS vào các thành phần, giúp dễ dàng định kiểu cho ứng dụng mà không cần cấu hình bổ sung.

### Next.js trong dự án

**Phiên bản**: Next.js 14.2.33

Dự án sử dụng Next.js với App Router (phiên bản mới nhất) để xây dựng ứng dụng full-stack. Next.js được cấu hình với reactStrictMode bật để phát hiện lỗi tiềm ẩn, swcMinify bật để minify code nhanh hơn, và cho phép load images từ localhost và hoanghamobile.com. API URL được cấu hình từ environment variables với giá trị mặc định là localhost:8080/api.

**File-Based Routing trong dự án**:

Dự án sử dụng hệ thống routing dựa trên cấu trúc thư mục. Thư mục app chứa tất cả các routes:
- Thư mục admin chứa các trang dành cho admin như accounting (kế toán), products (sản phẩm)
- Trong accounting có các trang con như tax (thuế) và transactions (giao dịch)
- Thư mục products có dynamic route với [id] để hiển thị chi tiết sản phẩm
- Thư mục employee chứa các trang dành cho nhân viên như accounting và warehouse
- Mỗi section có file layout.tsx riêng để định nghĩa layout chung

**Server Component và Client Component**:

Next.js 14 phân biệt hai loại component:
- Server Components (mặc định): Render trên server, có thể fetch data trực tiếp, không cần 'use client' directive. Ví dụ trang AccountingPage có thể fetch dữ liệu ngay trong component mà không cần useEffect.
- Client Components: Cần thêm 'use client' directive ở đầu file, có thể sử dụng hooks như useState, useEffect, và xử lý các tương tác người dùng. Ví dụ AccountingDashboard component cần 'use client' vì sử dụng useState và useEffect để fetch data từ client side.

**Các tính năng Next.js được sử dụng**:
- **App Router**: Routing system mới với layouts và nested routes, cho phép tổ chức code tốt hơn
- **Server Components**: Components render trên server để tối ưu performance và SEO
- **Client Components**: Components tương tác với 'use client' directive cho các tính năng động
- **Image Optimization**: Next.js Image component tự động optimize images, lazy load, và responsive
- **Environment Variables**: Quản lý API URLs và configs một cách an toàn
- **Metadata API**: SEO optimization với metadata cho mỗi trang
- **Loading States**: File loading.tsx tự động hiển thị loading UI khi trang đang load
- **Error Handling**: File error.tsx tự động xử lý lỗi và hiển thị error boundaries

**Lợi ích của Next.js trong dự án**:
- **Performance**: SSR giúp trang load nhanh hơn, SEO tốt hơn vì content được render sẵn trên server
- **Developer Experience**: Hot reload giúp xem thay đổi ngay lập tức, TypeScript support tốt, file-based routing đơn giản
- **Production Ready**: Built-in optimization như code splitting, security headers, image optimization
- **Scalability**: Code splitting tự động, lazy loading components giúp ứng dụng scale tốt
- **Full-stack**: API routes cho phép viết backend logic đơn giản ngay trong Next.js

## 2. STYLING & UI

### 2.1. Tailwind CSS
**Phiên bản**: 3.3.5

Tailwind CSS là utility-first CSS framework được sử dụng cho toàn bộ styling:

**Ưu điểm**:
- **Utility Classes**: Styling nhanh với các class có sẵn
- **Responsive Design**: Mobile-first với breakpoints (sm, md, lg, xl)
- **Customization**: Extend theme với custom colors, fonts
- **Performance**: PurgeCSS tự động loại bỏ CSS không dùng

**Custom Theme**:

Dự án đã tùy chỉnh theme với các màu sắc chính như primary (màu chủ đạo từ cấp độ 50 đến 900), secondary (màu phụ), navy (màu xanh navy), và dark (màu tối). Font chữ mặc định được cấu hình là Inter, với fallback là system-ui và sans-serif để đảm bảo hiển thị tốt trên mọi thiết bị.

### 2.2. CSS Modules & Global Styles
- **globals.css**: Global styles và Tailwind directives
- **PostCSS**: Xử lý CSS với autoprefixer
- **CSS-in-JS**: Styled-jsx được tích hợp sẵn trong Next.js

## 3. STATE MANAGEMENT

### 3.1. Zustand
**Phiên bản**: 4.4.7

Zustand là thư viện state management nhẹ và đơn giản:

**Stores được implement**:
- **authStore**: Quản lý authentication state (user, token, isAuthenticated)
- **cartStore**: Quản lý giỏ hàng
- **languageStore**: Quản lý đa ngôn ngữ

**Tính năng**:
- **Persist Middleware**: Lưu state vào localStorage
- **TypeScript Support**: Type-safe state management
- **Simple API**: Không cần Provider, Context
- **Performance**: Re-render tối ưu

**Ví dụ authStore**:

AuthStore trong dự án quản lý thông tin xác thực người dùng bao gồm: đối tượng user (có thể null nếu chưa đăng nhập), token JWT (có thể null), trạng thái isAuthenticated (true/false), và các hàm login để đăng nhập, logout để đăng xuất, setAuth để thiết lập thông tin xác thực. Store này được persist vào localStorage để giữ trạng thái đăng nhập ngay cả khi refresh trang.

## 4. HTTP CLIENT & API

### 4.1. Axios
**Phiên bản**: 1.6.0

Axios là HTTP client chính để gọi API:
- **Interceptors**: Tự động thêm Authorization header
- **Error Handling**: Xử lý lỗi tập trung
- **TypeScript**: Type-safe API calls
- **Base URL**: Cấu hình API_URL từ environment

**API Integration**:

Axios được cấu hình với baseURL trỏ đến API backend (mặc định là localhost:8080/api). Tất cả các request đều tự động thêm Authorization header với JWT token từ authStore. Khi có lỗi xảy ra, axios interceptor sẽ xử lý tập trung, ví dụ tự động logout nếu token hết hạn (401), hoặc hiển thị thông báo lỗi cho người dùng.

## 5. UI COMPONENTS & LIBRARIES

### 5.1. React Icons
**Phiên bản**: 4.12.0
- Icon library với hơn 10,000+ icons
- Tree-shakeable (chỉ import icons cần dùng)
- Sử dụng: FiUser, FiShoppingCart, FiMenu, etc.

### 5.2. React Hot Toast
**Phiên bản**: 2.4.1
- Toast notifications đẹp và dễ sử dụng
- Customizable styling
- Promise-based API

### 5.3. Sonner
**Phiên bản**: 2.0.7
- Alternative toast library
- Minimal và performant

### 5.4. Swiper
**Phiên bản**: 11.0.0
- Carousel/slider component
- Touch-enabled
- Responsive và customizable

### 5.5. React to Print
**Phiên bản**: 3.2.0
- In tài liệu/hóa đơn từ React components
- Sử dụng cho in đơn hàng, báo cáo

## 6. UTILITIES & TOOLS

### 6.1. HTML5 QRCode
**Phiên bản**: 2.3.8
- Quét QR code từ camera
- Sử dụng cho warehouse management (quét serial)

### 6.2. XLSX
**Phiên bản**: 0.18.5
- Import/Export Excel files
- Sử dụng cho:
  - Import sản phẩm từ Excel
  - Export báo cáo kế toán
  - Import/Export warehouse data

## 7. DEVELOPMENT TOOLS

### 7.1. ESLint
**Phiên bản**: 8.52.0
- Linting JavaScript/TypeScript code
- eslint-config-next: Next.js specific rules
- Đảm bảo code quality và consistency

### 7.2. PostCSS & Autoprefixer
- **PostCSS**: CSS transformation tool
- **Autoprefixer**: Tự động thêm vendor prefixes
- Đảm bảo CSS tương thích cross-browser

## 8. KIẾN TRÚC FRONTEND

### 8.1. Cấu trúc thư mục

Dự án frontend được tổ chức theo cấu trúc rõ ràng:
- Thư mục app chứa tất cả các pages theo Next.js App Router, bao gồm admin (trang quản trị), employee (trang nhân viên), cart (giỏ hàng), checkout (thanh toán), products (sản phẩm), và layout.tsx (layout gốc)
- Thư mục components chứa các reusable components được chia theo chức năng như admin components, layout components, product components
- Thư mục hooks chứa các custom React hooks như useAuth, usePermission
- Thư mục lib chứa các utilities và helper functions
- Thư mục store chứa các Zustand stores để quản lý state
- Thư mục styles chứa global styles và CSS
- Thư mục translations chứa các file dịch cho hệ thống đa ngôn ngữ

### 8.2. Routing Strategy
- **File-based Routing**: Mỗi file trong `app/` là một route
- **Dynamic Routes**: `[id]` cho dynamic segments
- **Route Groups**: `(group)` để organize routes
- **Nested Layouts**: Layout.tsx cho mỗi section

### 8.3. Authentication Flow
1. **Login**: POST /api/auth/login → Nhận token
2. **Store**: Lưu token vào Zustand + localStorage
3. **Protected Routes**: ProtectedRoute component check auth
4. **Role-based Access**: RoleBasedRedirect component
5. **Permission Guard**: PermissionGuard cho employee positions

### 8.4. Data Fetching Patterns
- **Client-side**: useEffect + axios trong components
- **Server Components**: Fetch data trực tiếp trong components
- **API Routes**: Next.js API routes cho proxy/middleware

## 9. FEATURES IMPLEMENTATION

### 9.1. Multi-language (i18n)
- Custom translation system
- languageStore quản lý ngôn ngữ hiện tại
- Translation files trong `translations/`
- useTranslation hook

### 9.2. Session Management
- BrowserSessionManager: Đồng bộ auth giữa tabs
- Tab synchronization với localStorage events
- Auto logout khi token expired

### 9.3. Permission System
- Role-based: ADMIN, EMPLOYEE, CUSTOMER
- Position-based: WAREHOUSE, ACCOUNTANT, SALE, etc.
- PermissionGuard component
- usePermissionCheck hook

### 9.4. Real-time Features
- Toast notifications cho user feedback
- Optimistic UI updates
- Loading states

## 10. PERFORMANCE OPTIMIZATION

### 10.1. Code Splitting
- Next.js automatic code splitting
- Dynamic imports cho heavy components
- Route-based splitting

### 10.2. Image Optimization
- Next.js Image component
- Lazy loading images
- Responsive images với srcset

### 10.3. Caching Strategy
- Zustand persist cho state
- localStorage cho auth & cart
- Browser caching cho static assets

### 10.4. Bundle Optimization
- SWC minification
- Tree shaking
- PurgeCSS với Tailwind

## 11. SECURITY

### 11.1. Authentication
- JWT token-based authentication
- Token stored in localStorage
- Auto-refresh mechanism

### 11.2. Authorization
- Role-based access control (RBAC)
- Position-based permissions
- Protected routes với guards

### 11.3. Content Security Policy
- CSP headers trong next.config.js
- Restrict script sources
- Prevent XSS attacks

## 12. BROWSER COMPATIBILITY

### 12.1. Target Browsers
- Modern browsers (Chrome, Firefox, Safari, Edge)
- ES2020 target
- Autoprefixer cho CSS compatibility

### 12.2. Polyfills
- Next.js tự động thêm polyfills cần thiết
- Core-js cho older browsers

## KẾT LUẬN

Frontend của dự án được xây dựng với stack công nghệ hiện đại:
- **Next.js 14** với App Router cho SSR/SSG
- **TypeScript** cho type safety
- **Tailwind CSS** cho styling nhanh và responsive
- **Zustand** cho state management đơn giản
- **Axios** cho API integration

Stack này đảm bảo:
✅ **Performance**: SSR, code splitting, image optimization
✅ **Developer Experience**: TypeScript, hot reload, ESLint
✅ **Maintainability**: Component-based, type-safe, organized structure
✅ **Scalability**: Modular architecture, reusable components
✅ **User Experience**: Fast loading, responsive, real-time feedback
