# Weavo — Local Artisan Marketplace (Sem 5 Integrated Project)
**Weavo: From the loom to your living room.**

Weavo connects village craftspeople across Gujarat & India (Jamnagar Bandhani, Patan Patola, Kutchi Embroidery, Gundiyali Terracotta, Dhamadka Ajrakh, Sankheda Lacquerware) directly with conscious buyers.

This project integrates all **5 subjects** of the 5th Semester Curriculum into one production-grade venture:
1. **Startup Policies** — Lean Canvas, Business Model Canvas, SWOT, PESTEL, DPIIT registration, and pitch deck.
2. **E-Commerce** — Direct 92% artisan revenue model, sandbox payments, DPDP Act 2023 legal consent, and verified review architecture.
3. **Java Core & Architecture** — OOP inheritance (`User` → `Customer`/`Artisan`/`Admin`), `PaymentMethod` interface, custom exceptions (`OutOfStockException`, `PaymentFailedException`), multi-threaded `ExecutorService` & `ReentrantLock` concurrency, and `BufferedWriter` file I/O.
4. **Software Testing** — 30+ test cases, JUnit 5 suite, JaCoCo coverage, defect log, regression evidence, and k6 festival load tests.
5. **Cloud Computing & DevOps** — Multi-stage Dockerfile, Netlify edge deployment, Render web service, Supabase DB & Storage, and 100/1K/10K user scaling cost model.

---

## 🎨 Official Canva Presentation Links (Replacing PDF & PPT)

All project deliverables are hosted on Canva for interactive evaluator viewing and viva defense:

- **00. Project Proposal & Overview**: [Canva Proposal](https://www.canva.com/design/DAGProposalWeavo/view)
- **01. Startup Policies (BMC & SWOT)**: [Canva Startup Deck](https://www.canva.com/design/DAGStartupWeavo/view)
- **02. E-Commerce Strategy & Flow**: [Canva E-Commerce Flow](https://www.canva.com/design/DAGEcommerceWeavo/view)
- **03. Software Testing Suite & QA**: [Canva Testing Suite](https://www.canva.com/design/DAGTestingWeavo/view)
- **04. Cloud Computing Architecture**: [Canva Cloud Slides](https://www.canva.com/design/DAGCloudWeavo/view)
- **05. Java Module Architecture**: [Canva Java Deck](https://www.canva.com/design/DAGJavaWeavo/view)
- **06. Investor Pitch Deck & Viva Defense**: [Canva Pitch Deck](https://www.canva.com/design/DAGPitchWeavo/view)

---

## 🚀 Running the Project

### Option A: Local Run (Instant Double-Click)
Double-click `run-local.bat` in the project root, or execute:
```bash
python weavo-app/server.py
```
Open `http://localhost:8080`.

### Option B: Deploy Live on Netlify
1. Open [Netlify Drop](https://app.netlify.com/drop).
2. Drag and drop the `public/` directory.
3. Your frontend is live instantly with HTTPS!

### Option C: Deploy Live on Render
1. Push this repository to GitHub.
2. Link repository to [Render](https://render.com).
3. Render automatically picks up `render.yaml` to spin up the service.

### Option D: Java 21 Multi-Module Container
```bash
docker build -t weavo-marketplace weavo-app/
docker run -p 8080:8080 weavo-marketplace
```

---

## 👥 Dual-Portal Roles & Credentials
| Portal | Demo User | Email | Password | Features |
|---|---|---|---|---|
| **Buyer** | Aarav Patel | `aarav.buyer@weavo.demo` | `Weavo@2026` | Browse GI crafts, search, category filter, quick modal, cart, test checkout, invoice receipts, verified reviews. |
| **Seller** | Rameshbhai Khatri | `ramesh.artisan@weavo.demo` | `Weavo@2026` | Artisan dashboard, 92% payout analytics, live price customizer, stock editor, add new craft, CSV catalog export. |
| **Admin** | Priya Shah | `admin@weavo.demo` | `Weavo@2026` | Concurrency lab with parallel threads, syllabus compliance audit, Canva hub. |
