import sys
import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE

def create_deck():
    prs = Presentation()
    # 16:9 Widescreen dimensions
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Brand Colors
    NAVY = RGBColor(0x17, 0x16, 0x38)
    DARK_CARD = RGBColor(0x22, 0x20, 0x4A)
    GOLD = RGBColor(0xF2, 0xB5, 0x44)
    TERRACOTTA = RGBColor(0xC6, 0x5D, 0x21)
    IVORY = RGBColor(0xF6, 0xF5, 0xF1)
    WHITE = RGBColor(0xFF, 0xFF, 0xFF)
    TEXT_MUTED = RGBColor(0x64, 0x74, 0x8B)
    GREEN = RGBColor(0x22, 0x7A, 0x54)
    BORDER_COLOR = RGBColor(0xE5, 0xE3, 0xDC)
    ROW_BG1 = RGBColor(0xEA, 0xF2, 0xEE)
    ROW_BG2 = RGBColor(0xF4, 0xF7, 0xF5)

    LIVE_URL = "https://weavo-village.netlify.app/"

    def set_slide_background(slide, color):
        background = slide.background
        fill = background.fill
        fill.solid()
        fill.fore_color.rgb = color

    def add_card(slide, left, top, width, height, bg_color=WHITE, border_color=BORDER_COLOR):
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        shape.fill.solid()
        shape.fill.fore_color.rgb = bg_color
        if border_color:
            shape.line.color.rgb = border_color
            shape.line.width = Pt(1.2)
        else:
            shape.line.fill.background()
        return shape

    # =========================================================================
    # SLIDE 1: MANDATORY COVER PAGE (JG UNIVERSITY TEMPLATE)
    # =========================================================================
    s1 = prs.slides.add_slide(blank_layout)
    set_slide_background(s1, WHITE)

    # University Header Title
    title_box = s1.shapes.add_textbox(Inches(1.0), Inches(0.35), Inches(11.33), Inches(1.1))
    tf1 = title_box.text_frame
    tf1.word_wrap = True
    p1 = tf1.paragraphs[0]
    p1.text = "JG UNIVERSITY"
    p1.font.name = "Arial"
    p1.font.size = Pt(36)
    p1.font.bold = True
    p1.font.color.rgb = RGBColor(0xB8, 0x24, 0x20) # JG Red
    p1.alignment = PP_ALIGN.CENTER

    p1_sub = tf1.add_paragraph()
    p1_sub.text = "EXCELLENCE BY CHOICE"
    p1_sub.font.name = "Arial"
    p1_sub.font.size = Pt(15)
    p1_sub.font.bold = True
    p1_sub.font.color.rgb = NAVY
    p1_sub.alignment = PP_ALIGN.CENTER

    p1_aff = tf1.add_paragraph()
    p1_aff.text = "Sponsored by ASIA Charitable Trust · School of Engineering · Information Technology"
    p1_aff.font.name = "Arial"
    p1_aff.font.size = Pt(11)
    p1_aff.font.color.rgb = TEXT_MUTED
    p1_aff.alignment = PP_ALIGN.CENTER

    # Project Title + Live URL
    p_title_box = s1.shapes.add_textbox(Inches(1.0), Inches(1.65), Inches(11.33), Inches(1.2))
    ptf = p_title_box.text_frame
    ptf.word_wrap = True
    pp1 = ptf.paragraphs[0]
    pp1.text = "Weavo"
    pp1.font.name = "Montserrat"
    pp1.font.size = Pt(44)
    pp1.font.bold = True
    pp1.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B) # Teal
    pp1.alignment = PP_ALIGN.LEFT

    pp2 = ptf.add_paragraph()
    pp2.text = "Online Local Artisan Marketplace — From the loom to your living room"
    pp2.font.name = "Arial"
    pp2.font.size = Pt(17)
    pp2.font.color.rgb = RGBColor(0x1B, 0x82, 0x80)
    pp2.alignment = PP_ALIGN.LEFT

    # Live Website Badge on Cover
    live_badge = s1.shapes.add_textbox(Inches(1.0), Inches(2.75), Inches(11.33), Inches(0.4))
    lbtf = live_badge.text_frame
    lbp = lbtf.paragraphs[0]
    lbp.text = f"🌐 Live Website: {LIVE_URL}"
    lbp.font.name = "Arial"
    lbp.font.size = Pt(13)
    lbp.font.bold = True
    lbp.font.color.rgb = TERRACOTTA

    # Submitted By label
    lbl_box = s1.shapes.add_textbox(Inches(1.0), Inches(3.2), Inches(3.0), Inches(0.5))
    ltf = lbl_box.text_frame
    lp = ltf.paragraphs[0]
    lp.text = "Submitted by :"
    lp.font.name = "Arial"
    lp.font.size = Pt(16)
    lp.font.bold = True
    lp.font.color.rgb = TEXT_MUTED

    # Table of 5 Members
    rows, cols = 6, 3
    t_left = Inches(3.8)
    t_top = Inches(3.2)
    t_width = Inches(5.8)
    t_height = Inches(2.4)

    table_shape = s1.shapes.add_table(rows, cols, t_left, t_top, t_width, t_height)
    table = table_shape.table
    table.columns[0].width = Inches(0.8)
    table.columns[1].width = Inches(3.2)
    table.columns[2].width = Inches(1.8)

    headers = ["No", "Name", "Roll number"]
    for c_idx, h_text in enumerate(headers):
        cell = table.cell(0, c_idx)
        cell.fill.solid()
        cell.fill.fore_color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        p = cell.text_frame.paragraphs[0]
        p.text = h_text
        p.font.name = "Arial"
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = WHITE
        p.alignment = PP_ALIGN.LEFT if c_idx > 0 else PP_ALIGN.CENTER

    members = [
        ("1", "Eklavya Desai", "242355"),
        ("2", "Faizankhan Pathan", "242432"),
        ("3", "Krish Padsala", "242397"),
        ("4", "Fenil Vora", "242473"),
        ("5", "Nihar Sangani", "242448"),
    ]

    for r_idx, (num, name, roll) in enumerate(members, start=1):
        for c_idx, val in enumerate([num, name, roll]):
            cell = table.cell(r_idx, c_idx)
            cell.fill.solid()
            cell.fill.fore_color.rgb = ROW_BG1 if r_idx % 2 == 1 else ROW_BG2
            p = cell.text_frame.paragraphs[0]
            p.text = val
            p.font.name = "Arial"
            p.font.size = Pt(12)
            p.font.bold = (c_idx == 1)
            p.font.color.rgb = NAVY
            p.alignment = PP_ALIGN.LEFT if c_idx > 0 else PP_ALIGN.CENTER

    # Submitted To
    sub_box = s1.shapes.add_textbox(Inches(1.0), Inches(5.7), Inches(4.5), Inches(1.5))
    stf = sub_box.text_frame
    stf.word_wrap = True
    sp1 = stf.paragraphs[0]
    sp1.text = "Submitted to :"
    sp1.font.name = "Arial"
    sp1.font.size = Pt(13.5)
    sp1.font.bold = True
    sp1.font.color.rgb = TEXT_MUTED

    for prof in ["Prof. Ashish Sir", "Prof. Ajay Sir", "Prof. Hemlata Ma’am"]:
        pp = stf.add_paragraph()
        pp.text = prof
        pp.font.name = "Arial"
        pp.font.size = Pt(13.5)
        pp.font.bold = True
        pp.font.color.rgb = NAVY

    # Helper for standard content slide
    def create_standard_slide(slide_num_str, title_str):
        slide = prs.slides.add_slide(blank_layout)
        set_slide_background(slide, WHITE)

        header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.4), Inches(11.7), Inches(0.9))
        htf = header_box.text_frame
        htf.word_wrap = True
        hp1 = htf.paragraphs[0]
        hp1.text = f"S L I D E  {slide_num_str}"
        hp1.font.name = "Arial"
        hp1.font.size = Pt(11)
        hp1.font.bold = True
        hp1.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

        hp2 = htf.add_paragraph()
        hp2.text = title_str
        hp2.font.name = "Montserrat"
        hp2.font.size = Pt(28)
        hp2.font.bold = True
        hp2.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

        return slide

    # =========================================================================
    # SLIDE 2: BUSINESS IDEA & INTRODUCTION
    # =========================================================================
    s2 = create_standard_slide("0 1", "Business Idea and Introduction")
    c1 = add_card(s2, Inches(0.8), Inches(1.5), Inches(5.6), Inches(1.6))
    c1_tf = c1.text_frame
    c1_tf.word_wrap = True
    p = c1_tf.paragraphs[0]
    p.text = "💡  What we offer"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    p2 = c1_tf.add_paragraph()
    p2.text = "A direct-to-consumer digital marketplace connecting verified rural Gujarat artisans (Jamnagar Bandhani, Patan Patola, Ajrakh, Gundiyali Terracotta) with buyers."
    p2.font.size = Pt(12)
    p2.font.color.rgb = TEXT_MUTED

    c2 = add_card(s2, Inches(0.8), Inches(3.3), Inches(5.6), Inches(1.6))
    c2_tf = c2.text_frame
    c2_tf.word_wrap = True
    p = c2_tf.paragraphs[0]
    p.text = "🎯  Main purpose"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    p2 = c2_tf.add_paragraph()
    p2.text = "Eliminate exploitative middlemen so rural artisans keep 90–92% of the retail selling price, while providing authentic Geographical Indication (GI) certified crafts."
    p2.font.size = Pt(12)
    p2.font.color.rgb = TEXT_MUTED

    c3 = add_card(s2, Inches(0.8), Inches(5.1), Inches(5.6), Inches(1.6))
    c3_tf = c3.text_frame
    c3_tf.word_wrap = True
    p = c3_tf.paragraphs[0]
    p.text = "⭐  What makes it different"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    p2 = c3_tf.add_paragraph()
    p2.text = "Seamless guest browsing (no forced login to explore), checkout-authenticated order placement, artisan customizer, and DPDP Act 2023 legal consent."
    p2.font.size = Pt(12)
    p2.font.color.rgb = TEXT_MUTED

    # Right preview card
    r_card = add_card(s2, Inches(6.8), Inches(1.5), Inches(5.7), Inches(5.2), bg_color=IVORY)
    rtf = r_card.text_frame
    rtf.word_wrap = True
    rp1 = rtf.paragraphs[0]
    rp1.text = "WEAVO ARTISAN MARKETPLACE"
    rp1.font.bold = True
    rp1.font.size = Pt(16)
    rp1.font.color.rgb = NAVY

    rp_live = rtf.add_paragraph()
    rp_live.text = f"Live Website: {LIVE_URL}"
    rp_live.font.bold = True
    rp_live.font.size = Pt(12)
    rp_live.font.color.rgb = TERRACOTTA

    rp2 = rtf.add_paragraph()
    rp2.text = "From the loom to your living room."
    rp2.font.italic = True
    rp2.font.size = Pt(13)
    rp2.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

    rp3 = rtf.add_paragraph()
    rp3.text = "\nFeatured Craft Lineages:\n• Jamnagar Gajji Silk Bandhani Saree — ₹4,850\n• Patan Double-Ikat Patola Silk Dupatta — ₹8,500\n• Dhamadka Natural Indigo Ajrakh Stole — ₹1,850\n• Gundiyali Hand-painted Terracotta Surahi — ₹920\n• Kutch Hand-embroidered Mirror Cushions — ₹1,450"
    rp3.font.size = Pt(12)
    rp3.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 3: PROBLEM IDENTIFICATION & IDEA GENERATION
    # =========================================================================
    s3 = create_standard_slide("0 2", "Problem Identification and Idea Generation")
    c1 = add_card(s3, Inches(0.8), Inches(1.5), Inches(3.6), Inches(3.5))
    c1_tf = c1.text_frame
    c1_tf.word_wrap = True
    p = c1_tf.paragraphs[0]
    p.text = "⚠️  The problem"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    for b in ["Middlemen take 50–60% margin", "Artisans earn below living wage", "Widespread powerloom counterfeits", "Buyers lack provenance proof"]:
        bp = c1_tf.add_paragraph()
        bp.text = f"• {b}"
        bp.font.size = Pt(12)
        bp.font.color.rgb = TEXT_MUTED

    c2 = add_card(s3, Inches(4.8), Inches(1.5), Inches(3.6), Inches(3.5))
    c2_tf = c2.text_frame
    c2_tf.word_wrap = True
    p = c2_tf.paragraphs[0]
    p.text = "👥  Who faces it"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    for b in ["200,000+ weavers across Gujarat", "Urban conscious ethnic wear buyers", "Handloom preservation foundations", "Corporate gifting coordinators"]:
        bp = c2_tf.add_paragraph()
        bp.text = f"• {b}"
        bp.font.size = Pt(12)
        bp.font.color.rgb = TEXT_MUTED

    c3 = add_card(s3, Inches(8.8), Inches(1.5), Inches(3.6), Inches(3.5))
    c3_tf = c3.text_frame
    c3_tf.word_wrap = True
    p = c3_tf.paragraphs[0]
    p.text = "💡  How the idea came"
    p.font.bold = True
    p.font.size = Pt(15)
    p.font.color.rgb = NAVY
    for b in ["Campus research in Kutch & Jamnagar", "High retail markups vs artisan pay", "GI Registry laws not digitized", "Need for transparent direct tech"]:
        bp = c3_tf.add_paragraph()
        bp.text = f"• {b}"
        bp.font.size = Pt(12)
        bp.font.color.rgb = TEXT_MUTED

    b_card = add_card(s3, Inches(0.8), Inches(5.3), Inches(11.6), Inches(1.5), bg_color=ROW_BG1)
    btf = b_card.text_frame
    btf.word_wrap = True
    bp = btf.paragraphs[0]
    bp.text = "🎯  Jobs-to-be-Done: \"When I buy authentic Indian heritage crafts, I want verifiable GI cluster origin and direct artisan compensation, so my purchase directly sustains rural craftsman families.\""
    bp.font.size = Pt(13)
    bp.font.bold = True
    bp.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

    bp2 = btf.add_paragraph()
    bp2.text = "Why a startup opportunity: 200,000+ Gujarat artisan families, booming conscious ethnic retail, 8–10% commission model, and natural cloud scaling for festival traffic."
    bp2.font.size = Pt(11.5)
    bp2.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 4: MARKET RESEARCH & OPPORTUNITY ANALYSIS
    # =========================================================================
    s4 = create_standard_slide("0 3", "Market Research and Opportunity Analysis")
    cards = [
        ("🎓  Target customers", ["Ethnic fashion seekers (22–45)", "Home interior & decor buyers", "NRI collectors seeking authentic GI", "Need: affordable, transparent, direct"], Inches(0.8), Inches(1.5)),
        ("👍  Strengths", ["Direct guest browsing experience", "92% direct artisan payout", "GI verification & zero fake review policy", "Java + Python cloud stack"], Inches(4.8), Inches(1.5)),
        ("⚠️  Weaknesses", ["Early brand awareness", "Artisan digital onboarding support", "Initial logistics dependency", "Test sandbox stage"], Inches(8.8), Inches(1.5)),
        ("⚖️  Competitors", ["Jaypore / FabIndia: high retail margins", "Amazon / Flipkart: unverified mass fakes", "Local exhibitions: seasonal and limited", "Direct artisan NGOs: weak tech UX"], Inches(0.8), Inches(4.0)),
        ("🚀  Opportunities", ["Rising D2C handloom demand", "Government DPIIT & ONDC integration", "Festival season volume spikes", "Corporate sustainable gifting"], Inches(4.8), Inches(4.0)),
        ("🛡️  Threats", ["Cheap synthetic machine knockoffs", "Return rates on fragile terracotta", "Raw material price fluctuations", "Logistics transit delays"], Inches(8.8), Inches(4.0)),
    ]
    for title, items, left, top in cards:
        c = add_card(s4, left, top, Inches(3.6), Inches(2.2))
        ctf = c.text_frame
        ctf.word_wrap = True
        p = ctf.paragraphs[0]
        p.text = title
        p.font.bold = True
        p.font.size = Pt(13.5)
        p.font.color.rgb = NAVY
        for it in items:
            p_sub = ctf.add_paragraph()
            p_sub.text = f"• {it}"
            p_sub.font.size = Pt(11)
            p_sub.font.color.rgb = TEXT_MUTED

    bot = s4.shapes.add_textbox(Inches(0.8), Inches(6.5), Inches(11.6), Inches(0.5))
    bot.text_frame.paragraphs[0].text = f"Our edge: 92% direct payout + GI verification + zero login barrier + deployed live at {LIVE_URL}"
    bot.text_frame.paragraphs[0].font.bold = True
    bot.text_frame.paragraphs[0].font.size = Pt(12)
    bot.text_frame.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

    # =========================================================================
    # SLIDE 5: BUSINESS MODEL CANVAS
    # =========================================================================
    s5 = create_standard_slide("0 4", "Business Model Canvas")
    bmc_blocks = [
        ("Key Partners", ["Weaver Cooperatives", "GI Registry Bodies", "India Post & Delhivery", "Test Payment Sandbox", "JG University Incubator"], Inches(0.8), Inches(1.4), Inches(2.1), Inches(3.8)),
        ("Key Activities", ["Artisan KYC Verification", "Platform & API Dev", "Concurrency Order Processing", "Craft Quality Audit", "Digital Marketing"], Inches(3.1), Inches(1.4), Inches(2.1), Inches(1.8)),
        ("Key Resources", ["Cloud Platform (Render/Netlify)", "Multi-Module Java Backend", "Verified Artisan Registry", "GI Authenticity Database", "Team of 5 Founders"], Inches(3.1), Inches(3.4), Inches(2.1), Inches(1.8)),
        ("Value Propositions", ["Artisans: 92% Direct Payout", "Buyers: Verifiable GI Crafts", "Live Price Customizer", "DPDP Act 2023 Compliance", "Transparent Tax Receipts"], Inches(5.4), Inches(1.4), Inches(2.3), Inches(3.8)),
        ("Customer Relations", ["Artisan Self-Service Portal", "Verified Buyer Reviews", "DPDP Privacy Transparency", "Order Tracking Receipts", "Support & Inquiries"], Inches(7.9), Inches(1.4), Inches(2.1), Inches(1.8)),
        ("Channels", ["Mobile-Friendly Web App", "Netlify Global Edge CDN", "Instagram / Reel Lineage Stories", "Campus Exhibitions", "Artisan Cluster Partnerships"], Inches(7.9), Inches(3.4), Inches(2.1), Inches(1.8)),
        ("Customer Segments", ["Conscious Handloom Buyers", "Artisan Weavers & Potters", "Wedding & Festive Shoppers", "Institutional Gifting Buyers", "Cultural Collectors"], Inches(10.2), Inches(1.4), Inches(2.2), Inches(3.8)),
    ]
    for title, items, left, top, w, h in bmc_blocks:
        card = add_card(s5, left, top, w, h)
        tf = card.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = title
        p.font.bold = True
        p.font.size = Pt(12)
        p.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        for it in items:
            ip = tf.add_paragraph()
            ip.text = f"• {it}"
            ip.font.size = Pt(10)
            ip.font.color.rgb = TEXT_MUTED

    c_cost = add_card(s5, Inches(0.8), Inches(5.4), Inches(5.6), Inches(1.4), bg_color=IVORY)
    ctf = c_cost.text_frame
    ctf.word_wrap = True
    ctf.paragraphs[0].text = "Cost Structure"
    ctf.paragraphs[0].font.bold = True
    ctf.paragraphs[0].font.size = Pt(12)
    ctf.paragraphs[0].font.color.rgb = NAVY
    cp = ctf.add_paragraph()
    cp.text = "Cloud hosting & DB (Render, Netlify, Supabase), packaging subsidies, artisan onboarding workshops, payment gateway test fee, logistics insurance."
    cp.font.size = Pt(10.5)
    cp.font.color.rgb = TEXT_MUTED

    c_rev = add_card(s5, Inches(6.8), Inches(5.4), Inches(5.6), Inches(1.4), bg_color=ROW_BG1)
    rtf = c_rev.text_frame
    rtf.word_wrap = True
    rtf.paragraphs[0].text = "Revenue Streams"
    rtf.paragraphs[0].font.bold = True
    rtf.paragraphs[0].font.size = Pt(12)
    rtf.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    rp = rtf.add_paragraph()
    rp.text = "8% Platform maintenance commission on completed sales, Karigar Pro artisan subscription tier (₹299/mo), and premium festival showcase placement."
    rp.font.size = Pt(10.5)
    rp.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 6: BUSINESS MODEL & REVENUE STRATEGY
    # =========================================================================
    s6 = create_standard_slide("0 5", "Business Model and Revenue Strategy")
    col1 = add_card(s6, Inches(0.8), Inches(1.5), Inches(3.6), Inches(2.6))
    col1_tf = col1.text_frame
    col1_tf.word_wrap = True
    col1_tf.paragraphs[0].text = "🔄  E-commerce model"
    col1_tf.paragraphs[0].font.bold = True
    col1_tf.paragraphs[0].font.size = Pt(14)
    col1_tf.paragraphs[0].font.color.rgb = NAVY
    for b in ["Direct C2C / B2C Artisan Marketplace", "Zero middleman distributor chain", "Artisans control price and stock"]:
        p = col1_tf.add_paragraph()
        p.text = f"• {b}"
        p.font.size = Pt(11.5)
        p.font.color.rgb = TEXT_MUTED

    col2 = add_card(s6, Inches(4.8), Inches(1.5), Inches(3.6), Inches(2.6))
    col2_tf = col2.text_frame
    col2_tf.word_wrap = True
    col2_tf.paragraphs[0].text = "🌐  Sales channel"
    col2_tf.paragraphs[0].font.bold = True
    col2_tf.paragraphs[0].font.size = Pt(14)
    col2_tf.paragraphs[0].font.color.rgb = NAVY
    for b in [f"Direct via {LIVE_URL}", "Netlify Global CDN distribution", "Verified digital identity & receipts"]:
        p = col2_tf.add_paragraph()
        p.text = f"• {b}"
        p.font.size = Pt(11.5)
        p.font.color.rgb = TEXT_MUTED

    col3 = add_card(s6, Inches(8.8), Inches(1.5), Inches(3.6), Inches(2.6))
    col3_tf = col3.text_frame
    col3_tf.word_wrap = True
    col3_tf.paragraphs[0].text = "💰  How we earn"
    col3_tf.paragraphs[0].font.bold = True
    col3_tf.paragraphs[0].font.size = Pt(14)
    col3_tf.paragraphs[0].font.color.rgb = NAVY
    for b in ["8% Platform commission on order", "Karigar Pro artisan membership", "Shipping logistics margin on <₹2.5K"]:
        p = col3_tf.add_paragraph()
        p.text = f"• {b}"
        p.font.size = Pt(11.5)
        p.font.color.rgb = TEXT_MUTED

    m1 = add_card(s6, Inches(0.8), Inches(4.5), Inches(3.6), Inches(1.5), bg_color=IVORY)
    m1_tf = m1.text_frame
    m1_tf.word_wrap = True
    m1_tf.paragraphs[0].text = "92% Direct"
    m1_tf.paragraphs[0].font.bold = True
    m1_tf.paragraphs[0].font.size = Pt(22)
    m1_tf.paragraphs[0].font.color.rgb = GREEN
    p = m1_tf.add_paragraph()
    p.text = "Direct payout to artisan bank account"
    p.font.size = Pt(11)
    p.font.color.rgb = TEXT_MUTED

    m2 = add_card(s6, Inches(4.8), Inches(4.5), Inches(3.6), Inches(1.5), bg_color=IVORY)
    m2_tf = m2.text_frame
    m2_tf.word_wrap = True
    m2_tf.paragraphs[0].text = "8% Fee"
    m2_tf.paragraphs[0].font.bold = True
    m2_tf.paragraphs[0].font.size = Pt(22)
    m2_tf.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    p = m2_tf.add_paragraph()
    p.text = "Platform hosting & maintenance"
    p.font.size = Pt(11)
    p.font.color.rgb = TEXT_MUTED

    m3 = add_card(s6, Inches(8.8), Inches(4.5), Inches(3.6), Inches(1.5), bg_color=IVORY)
    m3_tf = m3.text_frame
    m3_tf.word_wrap = True
    m3_tf.paragraphs[0].text = "₹299 / mo"
    m3_tf.paragraphs[0].font.bold = True
    m3_tf.paragraphs[0].font.size = Pt(22)
    m3_tf.paragraphs[0].font.color.rgb = TERRACOTTA
    p = m3_tf.add_paragraph()
    p.text = "Karigar Pro verified cluster tier"
    p.font.size = Pt(11)
    p.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 7: PRODUCT DEVELOPMENT & VALIDATION (MVP)
    # =========================================================================
    s7 = create_standard_slide("0 6", "Product Development and Validation")
    mvp_card = add_card(s7, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.2))
    mtf = mvp_card.text_frame
    mtf.word_wrap = True
    mtf.paragraphs[0].text = "Our Working MVP Platform"
    mtf.paragraphs[0].font.bold = True
    mtf.paragraphs[0].font.size = Pt(16)
    mtf.paragraphs[0].font.color.rgb = NAVY

    p_url = mtf.add_paragraph()
    p_url.text = f"Live Deployment: {LIVE_URL}"
    p_url.font.bold = True
    p_url.font.size = Pt(11.5)
    p_url.font.color.rgb = TERRACOTTA

    features = [
        "✓ Direct Customer Entry: No forced login; explore immediately",
        "✓ Checkout-Gated Order Auth: Seamless login on placing order",
        "✓ Artisan Live Customizer: Real-time price and stock editor",
        "✓ Sandbox Payments: UPI QR, Card & Wallet test gateway",
        "✓ Concurrency Lock: Parallel thread testing with zero overselling",
        "✓ DPDP Act 2023 Consent: Explicit legal compliance module",
        "✓ Java File I/O: Formatted text invoices & live CSV exports",
        "✓ Verified Reviews: Restricted strictly to confirmed buyers"
    ]
    for f in features:
        p = mtf.add_paragraph()
        p.text = f
        p.font.size = Pt(11.5)
        p.font.color.rgb = TEXT_MUTED

    r1 = add_card(s7, Inches(6.8), Inches(1.5), Inches(5.6), Inches(2.4), bg_color=IVORY)
    r1_tf = r1.text_frame
    r1_tf.word_wrap = True
    r1_tf.paragraphs[0].text = "🧪  Validation & Pilot Testing"
    r1_tf.paragraphs[0].font.bold = True
    r1_tf.paragraphs[0].font.size = Pt(14)
    r1_tf.paragraphs[0].font.color.rgb = NAVY
    for it in ["Campus survey across JG University students", "Interviews with 5 Kutch handloom artisans", "Simulated festival flash-sale order spikes (k6 load tests)", "Zero negative race condition occurrences"]:
        p = r1_tf.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED

    r2 = add_card(s7, Inches(6.8), Inches(4.3), Inches(5.6), Inches(2.4), bg_color=ROW_BG1)
    r2_tf = r2.text_frame
    r2_tf.word_wrap = True
    r2_tf.paragraphs[0].text = "🚀  Post-Viva Enhancements"
    r2_tf.paragraphs[0].font.bold = True
    r2_tf.paragraphs[0].font.size = Pt(14)
    r2_tf.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    for it in ["Production Razorpay payment gateway integration", "ONDC network node registration", "Mobile app for low-literacy rural artisans", "Automated India Post API barcode tracking"]:
        p = r2_tf.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 8: THE STARTUP TEAM & RESPONSIBILITIES
    # =========================================================================
    s8 = create_standard_slide("0 7", "Building the Startup Team")
    team = [
        ("Eklavya Desai", "242355", "Technology & Backend Architecture", ["Java 21 multi-module setup", "ExecutorService & ReentrantLock", "Custom exceptions handling", "Spring Boot API controllers"], Inches(0.8)),
        ("Faizankhan Pathan", "242432", "E-Commerce Strategy & Catalog", ["Platform user journey flow", "Craft catalogue classification", "Logistics & shipping model", "Packaging & delivery terms"], Inches(3.2)),
        ("Krish Padsala", "242397", "Full-Stack & Cloud Deployment", ["Modern responsive UI/UX", "Direct guest entry flow", "Render & Netlify deployment", "Python hybrid REST engine"], Inches(5.6)),
        ("Fenil Vora", "242473", "Software Testing & QA Suite", ["30+ JUnit 5 test cases", "JaCoCo statement & branch report", "k6 load spike tests", "Defect log & regression tests"], Inches(8.0)),
        ("Nihar Sangani", "242448", "Startup Policies & Compliance", ["Lean Canvas & BMC design", "SWOT & PESTEL analysis", "DPIIT registration note", "DPDP Act 2023 legal consent"], Inches(10.4)),
    ]
    for name, roll, role, tasks, left in team:
        card = add_card(s8, left, Inches(1.5), Inches(2.2), Inches(5.2))
        tf = card.text_frame
        tf.word_wrap = True
        p1 = tf.paragraphs[0]
        p1.text = name
        p1.font.bold = True
        p1.font.size = Pt(14)
        p1.font.color.rgb = NAVY

        p_roll = tf.add_paragraph()
        p_roll.text = f"Roll: {roll}"
        p_roll.font.size = Pt(11)
        p_roll.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

        p_role = tf.add_paragraph()
        p_role.text = role
        p_role.font.bold = True
        p_role.font.size = Pt(11)
        p_role.font.color.rgb = TERRACOTTA

        tf.add_paragraph()
        for t in tasks:
            tp = tf.add_paragraph()
            tp.text = f"• {t}"
            tp.font.size = Pt(10)
            tp.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 9: LEGAL STRUCTURE, REGISTRATION & POLICIES
    # =========================================================================
    s9 = create_standard_slide("0 8", "Legal Structure, Registration and Policies")
    leg_blocks = [
        ("🏢  Legal structure", ["Private Limited Company", "Limited liability protection", "Enables venture equity funding", "Board of 5 founding student directors"], Inches(0.8), Inches(1.5)),
        ("📜  Registrations & tax", ["Ministry of Corporate Affairs (MCA)", "PAN & TAN allocation", "GST registration (18% on platform fee)", "Startup India (DPIIT) registration"], Inches(6.8), Inches(1.5)),
        ("🏷️  Brand & IP protection", ["Weavo trademark (Classes 24 & 35)", "Copyright on software source code", "GI protection under GI of Goods Act 1999", "Domain ownership & SSL certs"], Inches(0.8), Inches(4.2)),
        ("🔒  Key policies & DPDP", ["DPDP Act 2023 explicit legal consent", "Strict zero dark patterns guarantee", "Artisan fair trade agreements", "Transparent return & dispute policy"], Inches(6.8), Inches(4.2)),
    ]
    for title, items, left, top in leg_blocks:
        card = add_card(s9, left, top, Inches(5.6), Inches(2.5))
        tf = card.text_frame
        tf.word_wrap = True
        tf.paragraphs[0].text = title
        tf.paragraphs[0].font.bold = True
        tf.paragraphs[0].font.size = Pt(14)
        tf.paragraphs[0].font.color.rgb = NAVY
        for it in items:
            p = tf.add_paragraph()
            p.text = f"• {it}"
            p.font.size = Pt(11.5)
            p.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 10: E-COMMERCE WEBSITE & ONLINE STORE SETUP
    # =========================================================================
    s10 = create_standard_slide("0 9", "E-Commerce Website and Online Store Setup")
    left_c = add_card(s10, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.2))
    ltf = left_c.text_frame
    ltf.word_wrap = True
    ltf.paragraphs[0].text = "Architecture & Interface Highlights"
    ltf.paragraphs[0].font.bold = True
    ltf.paragraphs[0].font.size = Pt(15)
    ltf.paragraphs[0].font.color.rgb = NAVY

    sections = [
        ("🌐 Live Domain & Hosting", f"Deployed live on Netlify Global CDN at {LIVE_URL} with Render backend API."),
        ("🚪 Direct Guest Access", "Visitors explore catalog, search, and view GI crafts instantly without forced signup."),
        ("📦 Verified Catalogue", "Product cards with cluster tags (Jamnagar, Patan, Bhujodi), GI certificates, live stock badges."),
        ("🎨 Live Customizer", "Artisan seller portal enables live price adjustment in ₹ and stock updates with immediate sync."),
        ("💳 Test Checkout", "Simulated UPI, Card & Wallet payments, DPDP 2023 consent, and Java BufferedWriter invoice generation.")
    ]
    for title, desc in sections:
        p1 = ltf.add_paragraph()
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(12)
        p1.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        p2 = ltf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(11)
        p2.font.color.rgb = TEXT_MUTED

    right_c = add_card(s10, Inches(6.8), Inches(1.5), Inches(5.6), Inches(5.2), bg_color=IVORY)
    rtf = right_c.text_frame
    rtf.word_wrap = True
    rtf.paragraphs[0].text = "WEAVO DUAL-PORTAL EXPERIENCE"
    rtf.paragraphs[0].font.bold = True
    rtf.paragraphs[0].font.size = Pt(14)
    rtf.paragraphs[0].font.color.rgb = NAVY

    rp = rtf.add_paragraph()
    rp.text = f"\n[ BUYER STOREFRONT — {LIVE_URL} ]\n• Direct storefront entry for buyers (no barrier)\n• Search & filter across 5 Gujarat craft categories\n• Quick-View craft provenance & artisan story\n• Free shipping above ₹2,500\n• Sign in upon simulated checkout\n\n[ ARTISAN SELLER PORTAL ]\n• Real-time sales and 92% payout KPI cards\n• Inline Price Customizer (edit price in ₹)\n• Stock quantity modifiers (+/-)\n• Publish new craft form with GI certificate\n• CSV Catalog export via Java BufferedWriter"
    rp.font.size = Pt(11)
    rp.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 11: PAYMENT, LOGISTICS & SUPPLY CHAIN
    # =========================================================================
    s11 = create_standard_slide("1 0", "Payment, Logistics and Supply Chain")
    steps = [
        ("Step 1", "Guest Browse", "Direct access to GI catalogue"),
        ("Step 2", "Checkout Auth", "Sign in on placing order"),
        ("Step 3", "Lock Stock", "Thread-safe stock decrement"),
        ("Step 4", "Loom Dispatch", "Artisan crafts & ships from village"),
        ("Step 5", "Delivery & Receipt", "Insured courier & tax invoice")
    ]
    for idx, (s_num, s_title, s_desc) in enumerate(steps):
        s_left = Inches(0.8 + idx * 2.35)
        sc = add_card(s11, s_left, Inches(1.5), Inches(2.2), Inches(2.0), bg_color=IVORY)
        stf = sc.text_frame
        stf.word_wrap = True
        stf.paragraphs[0].text = s_num
        stf.paragraphs[0].font.size = Pt(10)
        stf.paragraphs[0].font.color.rgb = TERRACOTTA
        p = stf.add_paragraph()
        p.text = s_title
        p.font.bold = True
        p.font.size = Pt(12)
        p.font.color.rgb = NAVY
        p2 = stf.add_paragraph()
        p2.text = s_desc
        p2.font.size = Pt(10)
        p2.font.color.rgb = TEXT_MUTED

    b_cards = [
        ("💳  Payment Gateway", "Sandbox mode with test UPI VPA and test cards. Demonstrates PaymentFailedException simulation."),
        ("📦  Inventory Control", "Physical handloom stock strictly managed. Atomic decrement prevents festival overselling."),
        ("🚚  Cluster Delivery", "Dispatched directly from Bhujodi, Jamnagar, and Patan via insured postal partners."),
        ("🔄  Reverse Logistics", "Fair return guarantee if craft is damaged during transit. 100% refund logged in ledger.")
    ]
    for idx, (b_title, b_desc) in enumerate(b_cards):
        b_left = Inches(0.8 + idx * 2.95)
        bc = add_card(s11, b_left, Inches(4.0), Inches(2.8), Inches(2.7))
        btf = bc.text_frame
        btf.word_wrap = True
        btf.paragraphs[0].text = b_title
        btf.paragraphs[0].font.bold = True
        btf.paragraphs[0].font.size = Pt(13)
        btf.paragraphs[0].font.color.rgb = NAVY
        p = btf.add_paragraph()
        p.text = b_desc
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 12: DIGITAL MARKETING & CUSTOMER ACQUISITION
    # =========================================================================
    s12 = create_standard_slide("1 1", "Digital Marketing and Customer Acquisition")
    m_left = add_card(s12, Inches(0.8), Inches(1.5), Inches(5.6), Inches(5.2))
    mtf = m_left.text_frame
    mtf.word_wrap = True
    mtf.paragraphs[0].text = "Marketing Strategy Channels"
    mtf.paragraphs[0].font.bold = True
    mtf.paragraphs[0].font.size = Pt(15)
    mtf.paragraphs[0].font.color.rgb = NAVY

    strat = [
        ("#1 Instagram & YouTube Shorts", "Behind-the-scenes pit loom weaving, Ajrakh block printing stages, and Bandhani knotting videos showing genuine artisan labor."),
        ("#2 Campus & Cultural Partnerships", "Student ambassador network across JG University and Ahmedabad colleges for sustainable fashion."),
        ("#3 Heritage Content & SEO", f"Authoritative blog articles on GI registry, pointing directly to {LIVE_URL} for verified crafts."),
        ("#4 Direct Artisan Storytelling", "Every product displays the artisan's name, village cluster, and percentage of earnings received.")
    ]
    for title, desc in strat:
        p1 = mtf.add_paragraph()
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(12)
        p1.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        p2 = mtf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(11)
        p2.font.color.rgb = TEXT_MUTED

    r1 = add_card(s12, Inches(6.8), Inches(1.5), Inches(5.6), Inches(2.4), bg_color=IVORY)
    r1.text_frame.word_wrap = True
    r1.text_frame.paragraphs[0].text = "🎯  Target Audience"
    r1.text_frame.paragraphs[0].font.bold = True
    r1.text_frame.paragraphs[0].font.size = Pt(14)
    r1.text_frame.paragraphs[0].font.color.rgb = NAVY
    for it in ["Urban Indian conscious consumers (22–45)", "Ahmedabad, Surat, Mumbai, and Delhi metro clusters", "Eco-conscious lifestyle and heritage enthusiasts", "Corporate procurement officers"]:
        p = r1.text_frame.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED

    r2 = add_card(s12, Inches(6.8), Inches(4.3), Inches(5.6), Inches(2.4), bg_color=ROW_BG1)
    r2.text_frame.word_wrap = True
    r2.text_frame.paragraphs[0].text = "🚀  Launch Campaign"
    r2.text_frame.paragraphs[0].font.bold = True
    r2.text_frame.paragraphs[0].font.size = Pt(14)
    r2.text_frame.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    for it in [f"Live website accessible at {LIVE_URL}", "Free insured craft delivery on orders over ₹2,500", "First-order artisan gratitude bookmark included", "Campus showcase during JG University TechFest"]:
        p = r2.text_frame.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 13: FINANCIAL PLANNING & STARTUP RESOURCES
    # =========================================================================
    s13 = create_standard_slide("1 2", "Financial Planning and Startup Resources")
    t_shape = s13.shapes.add_table(6, 2, Inches(0.8), Inches(1.5), Inches(5.6), Inches(3.2))
    table = t_shape.table
    table.columns[0].width = Inches(3.8)
    table.columns[1].width = Inches(1.8)

    t_headers = ["Initial Investment Item", "₹ (Approx.)"]
    for c_idx, h_text in enumerate(t_headers):
        cell = table.cell(0, c_idx)
        cell.fill.solid()
        cell.fill.fore_color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        p = cell.text_frame.paragraphs[0]
        p.text = h_text
        p.font.bold = True
        p.font.size = Pt(12)
        p.font.color.rgb = WHITE

    inv_rows = [
        ("Platform build & cloud infrastructure", "1,50,000"),
        ("Legal, MCA incorporation & DPIIT", "50,000"),
        ("Artisan onboarding & photography", "1,00,000"),
        ("Launch marketing & campus campaigns", "1,00,000"),
        ("Working capital reserve & contingency", "2,50,000"),
    ]
    for r_idx, (item, cost) in enumerate(inv_rows, start=1):
        for c_idx, val in enumerate([item, cost]):
            cell = table.cell(r_idx, c_idx)
            cell.fill.solid()
            cell.fill.fore_color.rgb = ROW_BG1 if r_idx % 2 == 1 else ROW_BG2
            p = cell.text_frame.paragraphs[0]
            p.text = val
            p.font.size = Pt(11.5)
            p.font.color.rgb = NAVY
            p.alignment = PP_ALIGN.LEFT if c_idx == 0 else PP_ALIGN.RIGHT

    t_box = s13.shapes.add_textbox(Inches(0.8), Inches(4.8), Inches(5.6), Inches(0.5))
    tp = t_box.text_frame.paragraphs[0]
    tp.text = "Total Initial Capital Required: ₹6,50,000"
    tp.font.bold = True
    tp.font.size = Pt(14)
    tp.font.color.rgb = NAVY

    r1 = add_card(s13, Inches(6.8), Inches(1.5), Inches(5.6), Inches(2.4), bg_color=IVORY)
    r1.text_frame.word_wrap = True
    r1.text_frame.paragraphs[0].text = "📊  Unit Economics & Assumptions"
    r1.text_frame.paragraphs[0].font.bold = True
    r1.text_frame.paragraphs[0].font.size = Pt(14)
    r1.text_frame.paragraphs[0].font.color.rgb = NAVY
    for it in ["Average Order Value (AOV): ₹3,200", "Platform revenue per order: ₹256 (8%)", "Artisan direct revenue: ₹2,944 (92%)", "Monthly operational overhead: ≈₹60,000", "Break-even target: Month 5 (approx. 240 orders/month)"]:
        p = r1.text_frame.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED

    r2 = add_card(s13, Inches(6.8), Inches(4.3), Inches(5.6), Inches(2.4), bg_color=ROW_BG1)
    r2.text_frame.word_wrap = True
    r2.text_frame.paragraphs[0].text = "💼  Funding Strategy"
    r2.text_frame.paragraphs[0].font.bold = True
    r2.text_frame.paragraphs[0].font.size = Pt(14)
    r2.text_frame.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    for it in ["Founders' personal savings (Bootstrapped)", "JG University Student Startup Incubator grant", "Startup India Seed Fund Scheme (SISFS)", "Gujarat State Handicrafts Development Corporation grant"]:
        p = r2.text_frame.add_paragraph()
        p.text = f"• {it}"
        p.font.size = Pt(11)
        p.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 14: JAVA IMPLEMENTATION & OOP CONCEPTS
    # =========================================================================
    s14 = create_standard_slide("1 3", "Java Core and OOP Architecture")
    java_concepts = [
        ("Classes & Objects", "User, CraftProduct, and Order model real-world artisanal entities as strongly typed state.", Inches(0.8), Inches(1.5)),
        ("Inheritance", "Abstract User base class extended by Customer, Artisan, and Admin roles.", Inches(3.8), Inches(1.5)),
        ("Encapsulation", "Private fields with validated getters/setters protecting craft inventory integrity.", Inches(6.8), Inches(1.5)),
        ("Polymorphism", "PaymentMethod interface implemented by UpiTestPayment, CardTestPayment, and WalletTestPayment.", Inches(9.8), Inches(1.5)),
        ("Custom Exceptions", "OutOfStockException and PaymentFailedException ensure graceful failure without crashing.", Inches(0.8), Inches(4.2)),
        ("Multithreading", "ExecutorService thread pool with ReentrantLock simulates concurrent checkout without race conditions.", Inches(3.8), Inches(4.2)),
        ("File Handling", "BufferedWriter exports live catalog to CSV and generates formatted text tax invoices.", Inches(6.8), Inches(4.2)),
        ("Spring Boot REST", "Layered Controller -> Service -> Repository design with Actuator health monitoring.", Inches(9.8), Inches(4.2)),
    ]
    for title, desc, left, top in java_concepts:
        card = add_card(s14, left, top, Inches(2.7), Inches(2.5))
        tf = card.text_frame
        tf.word_wrap = True
        tf.paragraphs[0].text = title
        tf.paragraphs[0].font.bold = True
        tf.paragraphs[0].font.size = Pt(13)
        tf.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
        p = tf.add_paragraph()
        p.text = desc
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 15: RISKS, ETHICS & SUSTAINABILITY
    # =========================================================================
    s15 = create_standard_slide("1 4", "Risks, Ethics and Sustainability")
    risks = [
        ("Risk: Fake or Powerloom Imitations", "Mitigation: Physical onboarding audit + verified GI certificate registration number displayed on each craft."),
        ("Risk: Low Initial Artisan Digital Literacy", "Mitigation: Simplified seller dashboard, voice assist, and local field cluster coordinators in Bhujodi."),
        ("Risk: Transit Breakage of Terracotta", "Mitigation: Eco-friendly honeycomb corrugated paper packaging and transit insurance coverage."),
        ("Risk: Concurrency Flash Sale Overselling", "Mitigation: ReentrantLock per-product locking and atomic stock decrement tested via k6."),
        ("Risk: Data Privacy Non-Compliance", "Mitigation: Built strictly under India's Digital Personal Data Protection (DPDP) Act 2023 with explicit user consent.")
    ]
    r_card = add_card(s15, Inches(0.8), Inches(1.5), Inches(6.6), Inches(5.2))
    rtf = r_card.text_frame
    rtf.word_wrap = True
    rtf.paragraphs[0].text = "Risk Matrix & Mitigations"
    rtf.paragraphs[0].font.bold = True
    rtf.paragraphs[0].font.size = Pt(15)
    rtf.paragraphs[0].font.color.rgb = NAVY
    for title, desc in risks:
        p1 = rtf.add_paragraph()
        p1.text = title
        p1.font.bold = True
        p1.font.size = Pt(11.5)
        p1.font.color.rgb = TERRACOTTA
        p2 = rtf.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(10.5)
        p2.font.color.rgb = TEXT_MUTED

    eth_card = add_card(s15, Inches(7.8), Inches(1.5), Inches(4.7), Inches(5.2), bg_color=ROW_BG1)
    etf = eth_card.text_frame
    etf.word_wrap = True
    etf.paragraphs[0].text = "🌿  Ethics & Sustainability Commitment"
    etf.paragraphs[0].font.bold = True
    etf.paragraphs[0].font.size = Pt(15)
    etf.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)

    points = [
        "100% Fair Trade: Artisans keep 92% of earnings.",
        "Zero Dark Patterns: No fake countdown timers, hidden fees, or trick checkboxes.",
        "Verified Reviews: Only customers with confirmed order numbers can post reviews.",
        "Environmental Impact: Organic rain-fed Kala cotton and natural azo-free vegetable dyes.",
        "Cultural Preservation: Safeguards 5,000 years of living Gujarati craft heritage."
    ]
    for pt in points:
        p = etf.add_paragraph()
        p.text = f"• {pt}"
        p.font.size = Pt(11.5)
        p.font.color.rgb = NAVY

    # =========================================================================
    # SLIDE 16: CONCLUSION & FUTURE VISION
    # =========================================================================
    s16 = create_standard_slide("1 5", "Conclusion and Future Vision")
    c1 = add_card(s16, Inches(0.8), Inches(1.5), Inches(5.6), Inches(2.2))
    c1.text_frame.word_wrap = True
    c1.text_frame.paragraphs[0].text = "🎯  The Opportunity"
    c1.text_frame.paragraphs[0].font.bold = True
    c1.text_frame.paragraphs[0].font.size = Pt(14)
    c1.text_frame.paragraphs[0].font.color.rgb = NAVY
    p = c1.text_frame.add_paragraph()
    p.text = "India's handicraft sector employs over 7 million rural artisans, yet lacks direct tech equity. Weavo bridges this divide by delivering verified GI crafts straight from loom to living room."
    p.font.size = Pt(11.5)
    p.font.color.rgb = TEXT_MUTED

    c2 = add_card(s16, Inches(6.8), Inches(1.5), Inches(5.6), Inches(2.2))
    c2.text_frame.word_wrap = True
    c2.text_frame.paragraphs[0].text = "✓  Why We Succeed"
    c2.text_frame.paragraphs[0].font.bold = True
    c2.text_frame.paragraphs[0].font.size = Pt(14)
    c2.text_frame.paragraphs[0].font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    p = c2.text_frame.add_paragraph()
    p.text = f"A fully working platform deployed live at {LIVE_URL}, combining modern frontend UI, thread-safe Java backend architecture, and a sustainable 92% artisan payout model."
    p.font.size = Pt(11.5)
    p.font.color.rgb = TEXT_MUTED

    c3 = add_card(s16, Inches(0.8), Inches(4.0), Inches(5.6), Inches(2.2))
    c3.text_frame.word_wrap = True
    c3.text_frame.paragraphs[0].text = "⭐  Key Technological Advantage"
    c3.text_frame.paragraphs[0].font.bold = True
    c3.text_frame.paragraphs[0].font.size = Pt(14)
    c3.text_frame.paragraphs[0].font.color.rgb = TERRACOTTA
    p = c3.text_frame.add_paragraph()
    p.text = "Direct storefront browsing with checkout-gated login, Live Price Customizer for artisans, and ExecutorService concurrency locks to guarantee zero overselling during festival spikes."
    p.font.size = Pt(11.5)
    p.font.color.rgb = TEXT_MUTED

    c4 = add_card(s16, Inches(6.8), Inches(4.0), Inches(5.6), Inches(2.2))
    c4.text_frame.word_wrap = True
    c4.text_frame.paragraphs[0].text = "🚩  Next Steps"
    c4.text_frame.paragraphs[0].font.bold = True
    c4.text_frame.paragraphs[0].font.size = Pt(14)
    c4.text_frame.paragraphs[0].font.color.rgb = NAVY
    p = c4.text_frame.add_paragraph()
    p.text = "Campus pilot launch at JG University, DPIIT startup registration, live Razorpay payment gateway integration, and onboarding 50 certified Gujarat handloom masters."
    p.font.size = Pt(11.5)
    p.font.color.rgb = TEXT_MUTED

    tag = s16.shapes.add_textbox(Inches(0.8), Inches(6.5), Inches(11.6), Inches(0.5))
    tp = tag.text_frame.paragraphs[0]
    tp.text = f"Weavo ({LIVE_URL}) — Empowering artisans, preserving heritage, and engineering fair trade for India."
    tp.font.bold = True
    tp.font.size = Pt(13)
    tp.font.color.rgb = RGBColor(0x0E, 0x5C, 0x5B)
    tp.alignment = PP_ALIGN.CENTER

    output_path = r"C:\college wp\DS practice project\Weavo_Sem5_Project\deliverables\Weavo_JG_University_Final_Presentation.pptx"
    prs.save(output_path)
    print(f"Presentation successfully updated with live URL {LIVE_URL} at {output_path}")

if __name__ == "__main__":
    create_deck()
