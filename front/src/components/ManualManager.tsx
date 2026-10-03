"use client";

import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";
import TypeBadge from "./TypeBadge";
import { EVALUATION_TYPE_LABEL, type EvaluationType, type ManualDoc } from "@/lib/api";

const TYPES = Object.keys(EVALUATION_TYPE_LABEL) as EvaluationType[];

interface FormState {
  id: number | null;
  evaluationType: EvaluationType;
  title: string;
  content: string;
  keywords: string;
}

const EMPTY: FormState = { id: null, evaluationType: "COMMON", title: "", content: "", keywords: "" };

export default function ManualManager({
  manuals,
  openNew = false,
}: {
  manuals: ManualDoc[];
  openNew?: boolean;
}) {
  const router = useRouter();
  const [form, setForm] = useState<FormState | null>(openNew ? EMPTY : null);
  const [filter, setFilter] = useState<EvaluationType | "ALL">("ALL");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const visible = manuals.filter((m) => filter === "ALL" || m.evaluationType === filter);

  function openEdit(m: ManualDoc) {
    setError(null);
    setForm({
      id: m.id,
      evaluationType: m.evaluationType,
      title: m.title,
      content: m.content,
      keywords: m.keywords ?? "",
    });
  }

  async function readError(res: Response, fallback: string) {
    const body = await res.json().catch(() => null);
    return body?.message ?? `${fallback} (${res.status})`;
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    if (!form) return;
    setSaving(true);
    setError(null);
    try {
      const res = await fetch(form.id == null ? "/api/manuals" : `/api/manuals/${form.id}`, {
        method: form.id == null ? "POST" : "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          evaluationType: form.evaluationType,
          title: form.title,
          content: form.content,
          keywords: form.keywords,
        }),
      });
      if (!res.ok) throw new Error(await readError(res, "저장 실패"));
      setForm(null);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "알 수 없는 오류");
    } finally {
      setSaving(false);
    }
  }

  async function onDelete(m: ManualDoc) {
    if (!window.confirm(`“${m.title}” 매뉴얼을 삭제할까요?\n삭제하면 되돌릴 수 없습니다.`)) return;
    const res = await fetch(`/api/manuals/${m.id}`, { method: "DELETE" });
    if (!res.ok) {
      window.alert(await readError(res, "삭제 실패"));
      return;
    }
    router.refresh();
  }

  return (
    <>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <div className="segmented">
          <button className={filter === "ALL" ? "active" : ""} onClick={() => setFilter("ALL")}>
            전체 {manuals.length}
          </button>
          {TYPES.map((t) => (
            <button
              key={t}
              className={filter === t ? "active" : ""}
              onClick={() => setFilter(t)}
            >
              {EVALUATION_TYPE_LABEL[t]} {manuals.filter((m) => m.evaluationType === t).length}
            </button>
          ))}
        </div>
        <button
          className="btn-ios"
          onClick={() => {
            setError(null);
            setForm(EMPTY);
          }}
        >
          + 매뉴얼 추가하기
        </button>
      </div>

      {visible.length === 0 ? (
        <div className="ios-card text-center" style={{ color: "var(--muted)", padding: 48 }}>
          등록된 매뉴얼이 없습니다.
        </div>
      ) : (
        <div className="inq-grid">
          {visible.map((m) => (
            <div key={m.id} className={`ios-card inq-card type-${m.evaluationType}`}>
              <div className="d-flex justify-content-between align-items-center">
                <TypeBadge type={m.evaluationType} />
                <span style={{ color: "var(--muted)", fontSize: "0.78rem" }}>#{m.id}</span>
              </div>
              <h3 className="inq-title">{m.title}</h3>
              <p className="inq-body">{m.content}</p>
              <div>
                {(m.keywords ?? "")
                  .split(",")
                  .map((k) => k.trim())
                  .filter(Boolean)
                  .map((k) => (
                    <span key={k} className="kw">
                      {k}
                    </span>
                  ))}
              </div>
              <div className="d-flex justify-content-end gap-2">
                <button className="btn-ios soft sm" onClick={() => openEdit(m)}>
                  수정
                </button>
                <button
                  className="btn-ios sm"
                  style={{ background: "rgba(255,59,48,0.12)", color: "#ff3b30" }}
                  onClick={() => onDelete(m)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {form && (
        <div className="sheet-backdrop" onClick={() => setForm(null)}>
          <form className="sheet" onClick={(e) => e.stopPropagation()} onSubmit={onSubmit}>
            <h4 style={{ fontWeight: 800, letterSpacing: "-0.02em" }}>
              {form.id == null ? "매뉴얼 추가" : "매뉴얼 수정"}
            </h4>

            <div className="mt-3 mb-3">
              <div className="card-label">평가유형</div>
              <select
                className="ios-input"
                value={form.evaluationType}
                onChange={(e) => setForm({ ...form, evaluationType: e.target.value as EvaluationType })}
              >
                {TYPES.map((t) => (
                  <option key={t} value={t}>
                    {EVALUATION_TYPE_LABEL[t]}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-3">
              <div className="card-label">제목</div>
              <input
                className="ios-input"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                required
              />
            </div>
            <div className="mb-3">
              <div className="card-label">내용</div>
              <textarea
                className="ios-input"
                rows={6}
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
                required
              />
            </div>
            <div className="mb-3">
              <div className="card-label">검색 키워드 (콤마로 구분)</div>
              <input
                className="ios-input"
                value={form.keywords}
                onChange={(e) => setForm({ ...form, keywords: e.target.value })}
                placeholder="예) 퇴사자, 평가자 삭제, 역량평가"
              />
            </div>

            {error && <p style={{ color: "#ff3b30" }}>{error}</p>}
            <div className="d-flex justify-content-end gap-2">
              <button type="button" className="btn-ios gray" onClick={() => setForm(null)}>
                취소
              </button>
              <button className="btn-ios" disabled={saving}>
                {saving ? "저장 중..." : "저장"}
              </button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
