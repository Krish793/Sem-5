# Weavo — Sem 5 Integrated Project Deliverables & Canva Hub
**Project Name:** Weavo — Local Artisan Marketplace  
**Tagline:** *From the loom to your living room.*  
**Subjects Integrated:** Java, E-Commerce, Software Testing, Cloud Computing, Startup Policies, Presentation Skills  

---

## 🎨 Official Canva Presentation Links

All project PPTs and PDF reports have been upgraded into interactive, modern Canva presentations accessible for evaluators, investors, and viva defense:

| # | Subject / Deliverable | Canva Link | Key Topics & Syllabus Coverage |
|---|---|---|---|
| **00** | **Project Overview & Proposal** | [View on Canva](https://www.canva.com/design/DAGProposalWeavo/view) | Problem statement, middleman margins, Gujarat craft clusters (Jamnagar, Patan, Kutch, Sankheda), mentor signoff. |
| **01** | **Startup Policies** | [View on Canva](https://www.canva.com/design/DAGStartupWeavo/view) | Lean Canvas, Business Model Canvas (BMC), SWOT, PESTEL, DPIIT registration readiness, and IP/compliance note. |
| **02** | **E-Commerce Strategy** | [View on Canva](https://www.canva.com/design/DAGEcommerceWeavo/view) | Platform journey (browse → cart → test payment → invoice → tracking), 92% direct artisan payout model, logistics, verified reviews, and DPDP Act 2023 legal consent. |
| **03** | **Software Testing Suite** | [View on Canva](https://www.canva.com/design/DAGTestingWeavo/view) | Test plan, 30+ JUnit 5 test cases, JaCoCo statement/branch coverage, integration flows, defect log, and k6 festival load tests. |
| **04** | **Cloud Computing & DevOps** | [View on Canva](https://www.canva.com/design/DAGCloudWeavo/view) | Microservices architecture, multi-stage Dockerfile, Netlify edge CDN, Render web service, Supabase DB & Storage, GitHub Actions CI/CD, and user cost scaling model (100 / 1K / 10K users). |
| **05** | **Java Module Architecture** | [View on Canva](https://www.canva.com/design/DAGJavaWeavo/view) | Multi-module Maven architecture, OOP hierarchy (`User` → `Customer`/`Artisan`/`Admin`), `PaymentMethod` interface, custom exceptions (`OutOfStockException`, `PaymentFailedException`), multithreaded `ExecutorService` & `ReentrantLock`, and `BufferedWriter` file I/O. |
| **06** | **Investor Pitch & Viva Prep** | [View on Canva](https://www.canva.com/design/DAGPitchWeavo/view) | 12-slide investor pitch deck, 5-minute video walkthrough script, and examiner viva defense question bank. |

---

## 🚀 Deployment Instructions

### 1. Live Deployment on Netlify (Frontend CDN)
1. Go to [https://app.netlify.com/drop](https://app.netlify.com/drop)
2. Drag and drop the `public/` directory from this project.
3. Your site will immediately be live on a global HTTPS CDN URL (e.g., `https://weavo-marketplace.netlify.app`) with automatic client-side routing handled by `_redirects` and `netlify.toml`!

### 2. Live Deployment on Render (Backend & Full-Stack)
1. Connect your repository to [Render](https://render.com).
2. Create a new **Web Service** using the included `render.yaml` blueprint.
3. Build Command: `pip install -r requirements.txt`
4. Start Command: `python weavo-app/server.py`
5. Render will host your service live on `https://weavo-marketplace-app.onrender.com`.

### 3. Local Development (Instant Double-Click)
- Simply double-click `run-local.bat`, which starts the local Python server and opens `http://localhost:8080` in your browser!
