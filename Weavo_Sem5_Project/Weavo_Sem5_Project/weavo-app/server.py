"""
Weavo Artisan Marketplace — Hybrid Python & Java Unified Backend Server
Complies with Sem 5 Integrated Project Requirements:
- Java OOP, Concurrency, and File I/O parity
- E-Commerce user journey (browse -> cart -> test payment -> invoice -> tracking)
- DPDP Act 2023 legal consent
- Dual-role authentication: Buyer (Customer) vs Seller (Artisan) + Admin
"""

import os
import sys
import uuid
import json
import time
import threading
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor
from flask import Flask, request, jsonify, send_from_directory, Response

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
STATIC_FOLDER = os.path.join(BASE_DIR, "kalasetu-web", "src", "main", "resources", "static")
app = Flask(__name__, static_folder=STATIC_FOLDER, static_url_path="")

# Lock for thread-safe in-memory order processing & stock decrement
STOCK_LOCK = threading.Lock()
NOTIFICATION_QUEUE = []
AUDIT_LOGS = []

# ==============================================================================
# SEED DATA
# ==============================================================================
CATEGORIES = [
    {
        "id": "11111111-1111-1111-1111-111111111101",
        "name": "Handloom & Textiles",
        "slug": "handloom-textiles",
        "description": "Traditional tie-dye Bandhani, Double Ikat Patola, and handwoven shawls",
        "regionFocus": "Jamnagar, Patan & Bhujodi, Gujarat"
    },
    {
        "id": "11111111-1111-1111-1111-111111111102",
        "name": "Block Printing & Natural Dyes",
        "slug": "block-printing",
        "description": "Hand-carved wooden block prints using natural indigo and madder",
        "regionFocus": "Dhamadka & Ajrakhpur, Kutch"
    },
    {
        "id": "11111111-1111-1111-1111-111111111103",
        "name": "Pottery & Terracotta",
        "slug": "pottery-terracotta",
        "description": "Wheel-thrown eco-friendly clay ware painted with natural mineral pigments",
        "regionFocus": "Gundiyali & Thangadh, Gujarat"
    },
    {
        "id": "11111111-1111-1111-1111-111111111104",
        "name": "Embroidery & Home Decor",
        "slug": "embroidery-decor",
        "description": "Kutchi mirror work, rabari stitch cushions, wall hangings, and torans",
        "regionFocus": "Kutch & Saurashtra, Gujarat"
    },
    {
        "id": "11111111-1111-1111-1111-111111111105",
        "name": "Woodcraft & Lacquerware",
        "slug": "woodcraft-lacquer",
        "description": "Lathe-turned teak wood artifacts coated in vibrant tin-foil lacquer",
        "regionFocus": "Sankheda & Idar, Gujarat"
    }
]

USERS = {
    "aarav.buyer@weavo.demo": {
        "id": "22222222-2222-2222-2222-222222222201",
        "fullName": "Aarav Patel",
        "email": "aarav.buyer@weavo.demo",
        "role": "CUSTOMER",
        "phone": "+91-9876500001",
        "city": "Ahmedabad",
        "state": "Gujarat",
        "dpdpConsent": True
    },
    "meera.buyer@weavo.demo": {
        "id": "22222222-2222-2222-2222-222222222202",
        "fullName": "Meera Shah",
        "email": "meera.buyer@weavo.demo",
        "role": "CUSTOMER",
        "phone": "+91-9876500002",
        "city": "Surat",
        "state": "Gujarat",
        "dpdpConsent": True
    },
    "ramesh.artisan@weavo.demo": {
        "id": "22222222-2222-2222-2222-222222222203",
        "fullName": "Rameshbhai Khatri",
        "email": "ramesh.artisan@weavo.demo",
        "role": "ARTISAN",
        "phone": "+91-9876500003",
        "city": "Jamnagar",
        "state": "Gujarat",
        "workshopName": "Khatri Bandhani & Ajrakh Kendra",
        "villageCluster": "Jamnagar Cluster",
        "giTag": "GI-GJ-BANDHANI-2019",
        "kycStatus": "VERIFIED",
        "commissionRate": 8.0,
        "dpdpConsent": True
    },
    "jiviben.artisan@weavo.demo": {
        "id": "22222222-2222-2222-2222-222222222204",
        "fullName": "Jiviben Ahir",
        "email": "jiviben.artisan@weavo.demo",
        "role": "ARTISAN",
        "phone": "+91-9876500004",
        "city": "Bhujodi",
        "state": "Gujarat",
        "workshopName": "Ahir Handloom Weavers Cooperative",
        "villageCluster": "Bhujodi Cluster, Kutch",
        "giTag": "GI-GJ-BHUJODI-2013",
        "kycStatus": "VERIFIED",
        "commissionRate": 8.0,
        "dpdpConsent": True
    },
    "admin@weavo.demo": {
        "id": "22222222-2222-2222-2222-222222222206",
        "fullName": "Priya Shah",
        "email": "admin@weavo.demo",
        "role": "ADMIN",
        "phone": "+91-9876500099",
        "city": "Ahmedabad",
        "state": "Gujarat",
        "dpdpConsent": True
    }
}

ARTISANS = [
    {
        "id": "33333333-3333-3333-3333-333333333301",
        "userId": "22222222-2222-2222-2222-222222222203",
        "workshopName": "Khatri Bandhani & Ajrakh Kala Kendra",
        "villageCluster": "Jamnagar Cluster",
        "district": "Jamnagar",
        "state": "Gujarat",
        "craftSpecialty": "Bandhani & Ajrakh",
        "giTagNumber": "GI-GJ-BANDHANI-2019",
        "kycStatus": "VERIFIED",
        "subscriptionTier": "KARIGAR_PRO",
        "commissionRate": 8.0
    },
    {
        "id": "33333333-3333-3333-3333-333333333302",
        "userId": "22222222-2222-2222-2222-222222222204",
        "workshopName": "Ahir Handloom Weavers Cooperative",
        "villageCluster": "Bhujodi Cluster",
        "district": "Kutch",
        "state": "Gujarat",
        "craftSpecialty": "Kala Cotton Shawls & Weaving",
        "giTagNumber": "GI-GJ-BHUJODI-2013",
        "kycStatus": "VERIFIED",
        "subscriptionTier": "KARIGAR_PRO",
        "commissionRate": 8.0
    }
]

PRODUCTS = [
    {
        "id": "44444444-4444-4444-4444-444444444401",
        "artisanId": "33333333-3333-3333-3333-333333333301",
        "categoryId": "11111111-1111-1111-1111-111111111101",
        "sku": "WV-BN-001",
        "title": "Jamnagar Gajji Silk Bandhani Saree",
        "craftType": "Bandhani Tie-Dye",
        "originVillage": "Jamnagar, Gujarat",
        "giCertified": True,
        "description": "Authentic hand-knotted 14,000 Ekdali dots on pure Gajji silk with rich zari border.",
        "priceInr": 4850.0,
        "stockQty": 12,
        "imageUrl": "/images/bandhani-saree.png",
        "featuredFestival": True,
        "active": True
    },
    {
        "id": "44444444-4444-4444-4444-444444444402",
        "artisanId": "33333333-3333-3333-3333-333333333301",
        "categoryId": "11111111-1111-1111-1111-111111111101",
        "sku": "WV-PT-002",
        "title": "Patan Double-Ikat Patola Silk Dupatta",
        "craftType": "Patola Silk Weaving",
        "originVillage": "Patan, Gujarat",
        "giCertified": True,
        "description": "Geometric Navratna motifs handwoven on pure Mulberry silk by master Salvi weavers.",
        "priceInr": 8500.0,
        "stockQty": 6,
        "imageUrl": "/images/patola-dupatta.png",
        "featuredFestival": True,
        "active": True
    },
    {
        "id": "44444444-4444-4444-4444-444444444403",
        "artisanId": "33333333-3333-3333-3333-333333333301",
        "categoryId": "11111111-1111-1111-1111-111111111102",
        "sku": "WV-AJ-003",
        "title": "Dhamadka Natural Indigo Ajrakh Modal Stole",
        "craftType": "Ajrakh Block Print",
        "originVillage": "Dhamadka, Kutch",
        "giCertified": True,
        "description": "16-stage hand block printing using natural indigo, harda, and madder root dyes.",
        "priceInr": 1850.0,
        "stockQty": 18,
        "imageUrl": "/images/ajrakh-stole.png",
        "featuredFestival": True,
        "active": True
    },
    {
        "id": "44444444-4444-4444-4444-444444444404",
        "artisanId": "33333333-3333-3333-3333-333333333301",
        "categoryId": "11111111-1111-1111-1111-111111111103",
        "sku": "WV-TR-004",
        "title": "Gundiyali Hand-painted Terracotta Water Surahi",
        "craftType": "Terracotta Craft",
        "originVillage": "Gundiyali, Kutch",
        "giCertified": False,
        "description": "Naturally cooling earthen clay flagon hand-turned and painted with geru and black geometric lines.",
        "priceInr": 920.0,
        "stockQty": 15,
        "imageUrl": "/images/terracotta-surahi.png",
        "featuredFestival": False,
        "active": True
    },
    {
        "id": "44444444-4444-4444-4444-444444444405",
        "artisanId": "33333333-3333-3333-3333-333333333302",
        "categoryId": "11111111-1111-1111-1111-111111111104",
        "sku": "WV-KT-005",
        "title": "Kutch Hand-embroidered Mirror Work Cushion Covers",
        "craftType": "Rabari & Ahir Embroidery",
        "originVillage": "Bhujodi, Kutch",
        "giCertified": True,
        "description": "Pair of organic Kala Cotton cushion covers with genuine hand-stitched Abhala mirror embellishments.",
        "priceInr": 1450.0,
        "stockQty": 20,
        "imageUrl": "/images/kutchi-cushions.png",
        "featuredFestival": True,
        "active": True
    },
    {
        "id": "44444444-4444-4444-4444-444444444406",
        "artisanId": "33333333-3333-3333-3333-333333333301",
        "categoryId": "11111111-1111-1111-1111-111111111105",
        "sku": "WV-SK-006",
        "title": "Sankheda Traditional Lacquerware Jewelry Box",
        "craftType": "Sankheda Lacquer Woodcraft",
        "originVillage": "Sankheda, Chhota Udaipur",
        "giCertified": True,
        "description": "Lathe-turned seasoned teakwood jewelry box coated with tin-foil golden lacquer and peacock motifs.",
        "priceInr": 2100.0,
        "stockQty": 8,
        "imageUrl": "/images/sankheda-box.png",
        "featuredFestival": False,
        "active": True
    }
]

CARTS = {}
ORDERS = {}
REVIEWS = []

# ==============================================================================
# REST API ENDPOINTS
# ==============================================================================

@app.route("/api/system/status", methods=["GET"])
def system_status():
    return jsonify({
        "status": "ONLINE",
        "applicationName": "Weavo Local Artisan Marketplace",
        "version": "1.0.0-Sem5",
        "academicMode": "ACADEMIC_TEST_MODE",
        "databaseMode": "SUPABASE_POSTGRES_COMPATIBLE",
        "paymentMode": "SANDBOX_SIMULATED",
        "compliance": "DPDP_ACT_2023_READY",
        "modules": [
            {"module": "kalasetu-domain", "status": "ACTIVE", "purpose": "Java OOP User hierarchy, Inheritance & Custom Exceptions"},
            {"module": "kalasetu-payment", "status": "ACTIVE", "purpose": "PaymentMethod Interface: UPI, Card, Wallet Sandbox"},
            {"module": "kalasetu-concurrency", "status": "ACTIVE", "purpose": "ExecutorService Order Processing & ReentrantLock"},
            {"module": "kalasetu-fileio", "status": "ACTIVE", "purpose": "CSV Catalog Export & Order Tax Invoice Receipts"},
            {"module": "kalasetu-persistence", "status": "ACTIVE", "purpose": "MarketplaceRepository with Atomic Stock Decrement"},
            {"module": "kalasetu-web", "status": "ACTIVE", "purpose": "Spring Boot 3 + Python Flask Unified REST API"}
        ]
    })

@app.route("/api/auth/demo-accounts", methods=["GET"])
def demo_accounts():
    return jsonify([
        {"email": "aarav.buyer@weavo.demo", "name": "Aarav Patel", "role": "CUSTOMER", "hint": "Weavo@2026"},
        {"email": "meera.buyer@weavo.demo", "name": "Meera Shah", "role": "CUSTOMER", "hint": "Weavo@2026"},
        {"email": "ramesh.artisan@weavo.demo", "name": "Rameshbhai Khatri", "role": "ARTISAN", "hint": "Weavo@2026"},
        {"email": "jiviben.artisan@weavo.demo", "name": "Jiviben Ahir", "role": "ARTISAN", "hint": "Weavo@2026"},
        {"email": "admin@weavo.demo", "name": "Priya Shah", "role": "ADMIN", "hint": "Weavo@2026"}
    ])

@app.route("/api/auth/login", methods=["POST"])
def auth_login():
    data = request.get_json() or {}
    email = data.get("email", "").strip().lower()
    user = USERS.get(email)
    if not user:
        # Create on the fly if non-demo customer
        user = {
            "id": str(uuid.uuid4()),
            "fullName": email.split("@")[0].capitalize(),
            "email": email,
            "role": "CUSTOMER",
            "phone": "+91-9999900000",
            "city": "Ahmedabad",
            "state": "Gujarat",
            "dpdpConsent": True
        }
        USERS[email] = user
    return jsonify({
        "token": "wv-mock-jwt-" + user["id"][:8],
        "user": user
    })

@app.route("/api/categories", methods=["GET"])
def list_categories():
    return jsonify(CATEGORIES)

@app.route("/api/products", methods=["GET"])
def list_products():
    return jsonify([p for p in PRODUCTS if p.get("active", True)])

@app.route("/api/products/search", methods=["GET"])
def search_products():
    q = request.args.get("q", "").strip().lower()
    cat_id = request.args.get("categoryId")
    gi_only = request.args.get("giOnly", "false").lower() == "true"

    res = []
    for p in PRODUCTS:
        if not p.get("active", True):
            continue
        if q and not (q in p["title"].lower() or q in p["craftType"].lower() or q in p["originVillage"].lower()):
            continue
        if cat_id and p["categoryId"] != cat_id:
            continue
        if gi_only and not p["giCertified"]:
            continue
        res.append(p)
    return jsonify(res)

@app.route("/api/products/<product_id>", methods=["GET"])
def get_product(product_id):
    p = next((x for x in PRODUCTS if x["id"] == product_id), None)
    if not p:
        return jsonify({"message": "Product not found"}), 404
    return jsonify(p)

# ==============================================================================
# SELLER / ARTISAN CUSTOMIZATION & ADD PRODUCT
# ==============================================================================
@app.route("/api/artisan/products", methods=["POST"])
def add_artisan_product():
    data = request.get_json() or {}
    new_p = {
        "id": str(uuid.uuid4()),
        "artisanId": data.get("artisanUserId", "33333333-3333-3333-3333-333333333301"),
        "categoryId": data.get("categoryId", CATEGORIES[0]["id"]),
        "sku": "WV-ART-" + str(uuid.uuid4())[:4].upper(),
        "title": data.get("title", "Handcrafted Artisan Specialty"),
        "craftType": data.get("craftType", "Handloom Weaving"),
        "originVillage": data.get("originVillage", "Gujarat Craft Cluster"),
        "giCertified": bool(data.get("giCertified", False)),
        "description": data.get("description", "Exquisite traditional craft made by rural artisans."),
        "priceInr": float(data.get("priceInr", 1500.0)),
        "stockQty": int(data.get("stockQty", 10)),
        "imageUrl": data.get("imageUrl") or "/images/weavo-ribbon-hero.svg",
        "featuredFestival": True,
        "active": True
    }
    with STOCK_LOCK:
        PRODUCTS.insert(0, new_p)
    return jsonify(new_p), 201

@app.route("/api/artisan/products/<product_id>", methods=["PATCH"])
def customize_artisan_product(product_id):
    """Allows artisan to customize product price, stock quantity, and active state"""
    data = request.get_json() or {}
    with STOCK_LOCK:
        p = next((x for x in PRODUCTS if x["id"] == product_id), None)
        if not p:
            return jsonify({"message": "Product not found"}), 404
        if "priceInr" in data:
            p["priceInr"] = float(data["priceInr"])
        if "stockQty" in data:
            p["stockQty"] = int(data["stockQty"])
        if "active" in data:
            p["active"] = bool(data["active"])
        if "title" in data:
            p["title"] = str(data["title"])
        if "description" in data:
            p["description"] = str(data["description"])
    return jsonify(p)

@app.route("/api/artisan/upload-image", methods=["POST"])
def upload_craft_image():
    # Return placeholder or local path for test mode
    return jsonify({"imageUrl": "/images/weavo-ribbon-hero.svg"})

# ==============================================================================
# CART & CHECKOUT
# ==============================================================================
@app.route("/api/cart/<customer_id>", methods=["GET"])
def get_cart(customer_id):
    return jsonify(CARTS.get(customer_id, []))

@app.route("/api/cart/<customer_id>", methods=["POST"])
def update_cart(customer_id):
    data = request.get_json() or {}
    product_id = data.get("productId")
    quantity = int(data.get("quantity", 1))

    items = CARTS.setdefault(customer_id, [])
    found = False
    for item in items:
        if item["productId"] == product_id:
            item["quantity"] = quantity
            found = True
            break
    if not found and quantity > 0:
        items.append({
            "id": str(uuid.uuid4()),
            "customerId": customer_id,
            "productId": product_id,
            "quantity": quantity
        })
    CARTS[customer_id] = [i for i in items if i["quantity"] > 0]
    return jsonify(CARTS[customer_id])

@app.route("/api/cart/<customer_id>/<product_id>", methods=["DELETE"])
def remove_cart_item(customer_id, product_id):
    items = CARTS.get(customer_id, [])
    CARTS[customer_id] = [i for i in items if i["productId"] != product_id]
    return jsonify({"success": True})

@app.route("/api/orders/checkout", methods=["POST"])
def checkout_order():
    data = request.get_json() or {}
    customer_id = data.get("customerId")
    simulate_fail = data.get("simulatePaymentFailure", False)
    dpdp_consent = data.get("dpdpConsentAccepted", False)

    if not dpdp_consent:
        return jsonify({"message": "DPDP Act 2023 consent is required for shipping fulfillment"}), 400

    if simulate_fail:
        return jsonify({"message": "PaymentFailedException: Sandbox payment simulation failed as requested"}), 402

    items = CARTS.get(customer_id, [])
    if not items:
        return jsonify({"message": "Your cart is empty"}), 400

    with STOCK_LOCK:
        # Atomic stock check and decrement
        total_amount = 0.0
        order_items = []
        for it in items:
            p = next((x for x in PRODUCTS if x["id"] == it["productId"]), None)
            if not p:
                return jsonify({"message": f"Product not found: {it['productId']}"}), 400
            if p["stockQty"] < it["quantity"]:
                return jsonify({"message": f"OutOfStockException: Only {p['stockQty']} units left for '{p['title']}'"}), 409
            p["stockQty"] -= it["quantity"]
            total_amount += p["priceInr"] * it["quantity"]
            order_items.append({
                "productId": p["id"],
                "title": p["title"],
                "quantity": it["quantity"],
                "price": p["priceInr"]
            })

        shipping_fee = 0.0 if total_amount >= 2500 else 99.0
        final_total = total_amount + shipping_fee
        order_number = f"WV-{int(time.time())}-{str(uuid.uuid4())[:4].upper()}"
        txn_ref = f"TXN-SANDBOX-{uuid.uuid4().hex[:10].upper()}"

        order = {
            "id": str(uuid.uuid4()),
            "orderNumber": order_number,
            "customerId": customer_id,
            "items": order_items,
            "subtotalInr": total_amount,
            "shippingFeeInr": shipping_fee,
            "totalAmountInr": final_total,
            "platformCommissionInr": round(total_amount * 0.08, 2),
            "artisanDirectPayoutInr": round(total_amount * 0.92, 2),
            "paymentStatus": "PAID_SANDBOX",
            "orderStatus": "CONFIRMED",
            "transactionRef": txn_ref,
            "shippingAddress": data.get("shippingAddress", "Navrangpura, Ahmedabad"),
            "createdAt": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }

        ORDERS.setdefault(customer_id, []).insert(0, order)
        CARTS[customer_id] = []

    return jsonify(order)

@app.route("/api/orders/customer/<customer_id>", methods=["GET"])
def get_customer_orders(customer_id):
    return jsonify(ORDERS.get(customer_id, []))

@app.route("/api/orders/<order_number>/receipt", methods=["GET"])
def get_order_receipt(order_number):
    found_order = None
    for ord_list in ORDERS.values():
        for o in ord_list:
            if o["orderNumber"] == order_number:
                found_order = o
                break
    if not found_order:
        return "Receipt not found", 404

    receipt = f"""================================================================
                    WEAVO ARTISAN MARKETPLACE
             From the loom to your living room.
================================================================
Order Number:     {found_order['orderNumber']}
Transaction Ref:  {found_order['transactionRef']}
Date:             {found_order['createdAt']}
Payment Mode:     SANDBOX SIMULATED (DPDP Act 2023 Compliant)
Status:           {found_order['orderStatus']} / {found_order['paymentStatus']}
----------------------------------------------------------------
ITEMS:
"""
    for item in found_order["items"]:
        receipt += f" - {item['title']} (x{item['quantity']}) @ Rs {item['price']:.2f}\n"

    receipt += f"""----------------------------------------------------------------
Subtotal:         Rs {found_order['subtotalInr']:.2f}
Shipping Fee:     Rs {found_order['shippingFeeInr']:.2f}
Total Paid:       Rs {found_order['totalAmountInr']:.2f}
----------------------------------------------------------------
COMMUNITY DIRECT IMPACT:
Artisan Direct Share (92%): Rs {found_order['artisanDirectPayoutInr']:.2f}
Platform Fee (8%):          Rs {found_order['platformCommissionInr']:.2f}
Ship to:          {found_order['shippingAddress']}
================================================================
Thank you for preserving India's living handloom heritage.
================================================================"""
    return Response(receipt, mimetype="text/plain")

# ==============================================================================
# CONCURRENCY LAB (Parity with Java kalasetu-concurrency)
# ==============================================================================
@app.route("/api/admin/concurrency-demo", methods=["POST"])
def concurrency_demo():
    """Simulates parallel buyer threads purchasing the same product to test atomic locks"""
    data = request.get_json() or {}
    product_id = data.get("productId")
    threads_count = min(max(int(data.get("threads", 6)), 2), 12)

    p = next((x for x in PRODUCTS if x["id"] == product_id), None)
    if not p:
        return jsonify({"message": "Product not found"}), 404

    initial_stock = p["stockQty"]
    results = []

    def buyer_thread_task(thread_idx):
        time.sleep(0.01 * thread_idx)
        with STOCK_LOCK:
            if p["stockQty"] >= 1:
                p["stockQty"] -= 1
                return {
                    "thread": f"BuyerThread-{thread_idx}",
                    "status": "SUCCESS",
                    "remainingStock": p["stockQty"],
                    "timestamp": time.time()
                }
            else:
                return {
                    "thread": f"BuyerThread-{thread_idx}",
                    "status": "FAILED_OUT_OF_STOCK",
                    "remainingStock": p["stockQty"],
                    "timestamp": time.time()
                }

    with ThreadPoolExecutor(max_workers=threads_count) as executor:
        futures = [executor.submit(buyer_thread_task, i + 1) for i in range(threads_count)]
        for f in futures:
            results.append(f.result())

    success_count = sum(1 for r in results if r["status"] == "SUCCESS")
    failed_count = sum(1 for r in results if r["status"] == "FAILED_OUT_OF_STOCK")

    return jsonify({
        "productId": product_id,
        "productTitle": p["title"],
        "initialStock": initial_stock,
        "finalStock": p["stockQty"],
        "threadsLaunched": threads_count,
        "successOrders": success_count,
        "rejectedOutOfStock": failed_count,
        "zeroOversellingVerified": (p["stockQty"] >= 0 and success_count <= initial_stock),
        "threadExecutionTrace": results
    })

# ==============================================================================
# CSV EXPORT (Parity with Java kalasetu-fileio)
# ==============================================================================
@app.route("/api/files/export/csv", methods=["GET"])
def export_csv():
    csv_rows = ["SKU,Title,CraftType,OriginVillage,PriceINR,StockQty,GICertified,Active"]
    for p in PRODUCTS:
        csv_rows.append(f'"{p["sku"]}","{p["title"]}","{p["craftType"]}","{p["originVillage"]}",{p["priceInr"]},{p["stockQty"]},{p["giCertified"]},{p.get("active", True)}')
    return Response("\n".join(csv_rows), mimetype="text/csv", headers={"Content-Disposition": "attachment; filename=weavo_craft_catalog.csv"})

# ==============================================================================
# STATIC FRONTEND ROUTES
# ==============================================================================
@app.route("/")
def serve_index():
    return send_from_directory(app.static_folder, "index.html")

@app.route("/<path:path>")
def serve_static(path):
    if os.path.exists(os.path.join(app.static_folder, path)):
        return send_from_directory(app.static_folder, path)
    return send_from_directory(app.static_folder, "index.html")

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    print(f"[*] Weavo Hybrid Backend running at http://0.0.0.0:{port}")
    app.run(host="0.0.0.0", port=port, debug=False)
